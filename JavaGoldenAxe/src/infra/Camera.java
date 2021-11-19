package infra;

import actor.Enemy;
import actor.Player;
import static infra.Settings.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import scene.Stage;

/**
 * Camera class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Camera {

    private static final Map<String, Integer> LOCKING_ACTORS = new HashMap<>();
    
    private static int activeEnemiesCount;
    private static int bossEnemiesCount;
    private static int notBossEnemiesCount;
    
    public static int getActiveEnemiesCount() {
        return activeEnemiesCount;
    }

    public static void incActiveEnemyCount() {
        activeEnemiesCount++;
    }
    
    public static int getBossEnemiesCount() {
        return bossEnemiesCount;
    }

    public static int getNotBossEnemiesCount() {
        return notBossEnemiesCount;
    }
    
    public static void addLockingActor(Actor actor) {
        if (actor.isLockingCamera()) {
            Integer count = LOCKING_ACTORS.get(actor.getLockingCameraId());
            if (count == null) {
                count = 0;
            }
            count++;
            if (actor instanceof Enemy enemy) {
                if (enemy.isBoss()) {
                    bossEnemiesCount++;
                }
                else {
                    notBossEnemiesCount++;
                }
            }
            LOCKING_ACTORS.put(actor.getLockingCameraId(), count);
        }
    }

    public static void removeLockingActor(Actor actor) {
        if (actor.isLockingCamera()) {
            Integer count = LOCKING_ACTORS.get(actor.getLockingCameraId());
            if (count == null) {
                actor.setLockingCameraId(null);
                return;
            }
            if (count > 0) {
                count--;
                if (actor instanceof Enemy enemy) {
                    if (!enemy.isMountable()) {
                        activeEnemiesCount--;
                    }
                    if (enemy.isBoss()) {
                        bossEnemiesCount--;
                    }
                    else {
                        notBossEnemiesCount--;
                    }
                }
            }
            LOCKING_ACTORS.put(actor.getLockingCameraId(), count);
            actor.setLockingCameraId(null);
        }
    }

    private static class LockInfo implements Comparable<LockInfo> {
        
        String cameraLockingId;
        int x;
        boolean nextStage;
        long nextStageWaitTime;
        boolean autoScroll;
        boolean playersMoveForced;
        boolean playersControlsEnabled;
        int playersMoveMargin;
        
        public LockInfo(String cameraLockingId, int x, boolean nextStage
                , long nextStageWaitTime, boolean autoScroll
                    , int playersMoveMargin) {
            
            this.cameraLockingId = cameraLockingId;
            this.x = x;
            this.nextStage = nextStage;
            this.nextStageWaitTime = nextStageWaitTime;
            this.autoScroll = autoScroll;
            this.playersMoveMargin = playersMoveMargin;
        }

        @Override
        public int compareTo(LockInfo o) {
            return x - o.x;
        }
        
    }
    
    private static final int PLAYER_HEIGHT = 48;
    private static final int PLAYER_WIDTH = 24;
    private static final int PLAYER_HALF_WIDTH = PLAYER_WIDTH / 2;
    private static final double FOLLOW_SPEED = 0.05;
    private static final List<LockInfo> LOCKS = new ArrayList<>();
    
    private static Stage currentStage;
    private static double x;
    private static double y;
    private static double targetMinX;
    private static double maxX = Double.MAX_VALUE;
    private static int[] minY;
    private static int[] maxY;
    private static LockInfo currentLock = null;
    private static boolean hardShakeEnabled;
    private static boolean smoothShakeEnabled;
    private static long nextStageTime;
    private static boolean nextStageCalled;
    private static int sameLocationX;
    private static int sameLocationCounter;
    private static Player player1;
    private static Player player2;
    private static boolean stageCleared;
    
    private Camera() {
    }

    public static boolean isStageCleared() {
        return stageCleared;
    }

    public static void reset(Stage currentStage) {
        Camera.currentStage = currentStage;
        x = 0.0;
        y = 0.0;
        targetMinX = 0.0;
        maxX = Double.MAX_VALUE;
        minY = null;
        maxY = null;
        LOCKING_ACTORS.clear();
        LOCKS.clear();
        currentLock = null;
        smoothShakeEnabled = false;
        hardShakeEnabled = false;
        nextStageTime = -1;
        nextStageCalled = false;
        sameLocationX = 0;
        sameLocationCounter = 0;
        player1 = null;
        player2 = null;
        stageCleared = false;
        activeEnemiesCount = 0;
        bossEnemiesCount = 0;
        notBossEnemiesCount = 0;        
    }

    public static void setPlayer1(Player player1) {
        Camera.player1 = player1;
    }

    public static void setPlayer2(Player player2) {
        Camera.player2 = player2;
    }

    public static double getX() {
        return x;
    }

    public static void setX(double x) {
        Camera.x = x;
    }

    public static double getY() {
        return y;
    }

    public static void setY(double y) {
        Camera.y = y;
    }

    public static void setMaxX(double maxX) {
        Camera.maxX = maxX;
    }

    public static int getMinY(int x) {
        if (minY == null || x < 0 || x > minY.length - 1) {
            return 0;
        }
        // System.out.printf("minY[%d] = %d\n", x, minY[x]);
        return minY[x];
    }

    public static void setMinY(String[] data) {
        int size = Integer.parseInt(data[data.length - 2]);
        minY = new int[size + 1];
        setMinOrMaxY(minY, data);
    }

    public static int getMaxY(int x) {
        if (maxY == null || x < 0 || x > maxY.length - 1) {
            return Integer.MAX_VALUE;
        }
        return maxY[x];
    }
    
    public static void setMaxY(String[] data) {
        int size = Integer.parseInt(data[data.length - 2]);
        maxY = new int[size + 1];
        setMinOrMaxY(maxY, data);
    }
    
    private static void setMinOrMaxY(int[] ys, String[] data) {
        int x1 = Integer.parseInt(data[1]);
        int y1 = Integer.parseInt(data[2]);
        for (int i = 3; i < data.length; i += 2) {
            int x2 = Integer.parseInt(data[i]);
            int y2 = Integer.parseInt(data[i + 1]);
            double dx = x2 - x1;
            double dy = y2 - y1;
            double ny = 0.0;
            if (dx != 0) {
                ny = dy / dx;
            }
            for (int i2 = x1; i2 <= x2; i2++) {
                ys[i2] = (int) (y1 + ny * (i2 - x1));
            }
            x1 = x2;
            y1 = y2;
        }
    }
    
    public static void addLock(String cameraLockingId, int x
            , boolean nextStage, long nextStageWaitTime
                , boolean autoScroll, int playersMoveMargin) {
        
        LOCKS.add(new LockInfo(
                cameraLockingId, x, nextStage, nextStageWaitTime
                    , autoScroll, playersMoveMargin));
        
        Collections.sort(LOCKS);
    }
    
    public static boolean isSmoothShakeEnabled() {
        return smoothShakeEnabled;
    }

    public static void setSmoothShakeEnabled(boolean smoothShakeEnabled) {
        Camera.smoothShakeEnabled = smoothShakeEnabled;
    }

    public static boolean isHardShakeEnabled() {
        return hardShakeEnabled;
    }

    public static void setHardShakeEnabled(boolean hardShakeEnabled) {
        Camera.hardShakeEnabled = hardShakeEnabled;
    }
    
    private static double getPlayer1X(double invalidValue){
        if (player1 != null && player1.isAlive()) {
            return player1.getLastValidWx();
        }
        return invalidValue;
    }

    private static double getPlayer2X(double invalidValue){
        if (player2 != null && player2.isAlive()) {
            return player2.getLastValidWx();
        }
        return invalidValue;
    }

    private static double getPlayer1FloorHeight(double invalidValue){
        if (player1 != null && player1.isAlive()) {
            return player1.getFloorHeight();
        }
        return invalidValue;
    }

    private static double getPlayer2FloorHeight(double invalidValue){
        if (player2 != null && player2.isAlive()) {
            return player2.getFloorHeight();
        }
        return invalidValue;
    }
        
    private static double getPlayer1Z(double invalidValue){
        if (player1 != null && player1.isAlive()) {
            return player1.getLastValidWz();
        }
        return invalidValue;
    }

    private static double getPlayer2Z(double invalidValue){
        if (player2 != null && player2.isAlive()) {
            return player2.getLastValidWz();
        }
        return invalidValue;
    }
    
    public static double getPlayerMinX() {
        return x + PLAYER_HALF_WIDTH;
    }

    public static double getPlayerMaxX() {
        return x + CANVAS_WIDTH - PLAYER_HALF_WIDTH;
    }

    public static double getPlayerMinZ() {
        double minValue = -Double.MAX_VALUE;
        double p1h = getPlayer1Z(minValue) + getPlayer1FloorHeight(minValue);
        double p2h = getPlayer2Z(minValue) + getPlayer2FloorHeight(minValue);

        if (getPlayer1FloorHeight(minValue) >= Terrain.MAX_HEIGHT) {
            p1h = p2h;
        }
        if (getPlayer2FloorHeight(minValue) >= Terrain.MAX_HEIGHT) {
            p2h = p1h;
        }

        return p1h > p2h ? p1h - CANVAS_HEIGHT + PLAYER_HEIGHT 
                                : p2h - CANVAS_HEIGHT + PLAYER_HEIGHT;
    }

    public static double getPlayerMaxZ() {
        double maxValue = Double.MAX_VALUE;
        double p1h = getPlayer1Z(maxValue) + getPlayer1FloorHeight(maxValue);
        double p2h = getPlayer2Z(maxValue) + getPlayer2FloorHeight(maxValue);
        
        if (getPlayer1FloorHeight(maxValue) >= Terrain.MAX_HEIGHT) {
            p1h = p2h;
        }
        if (getPlayer2FloorHeight(maxValue) >= Terrain.MAX_HEIGHT) {
            p2h = p1h;
        }

        return p1h > p2h ? p2h + CANVAS_HEIGHT - PLAYER_HEIGHT 
                                : p1h + CANVAS_HEIGHT - PLAYER_HEIGHT;
    }
        
    public static void update() {
        updateLock();
        updateFollowPlayers();
    }
    
    private static void updateLock() {
        if (currentLock != null) {
            Integer count = LOCKING_ACTORS.get(currentLock.cameraLockingId);
            if (count == null || count <= 0) {
                if (!currentLock.nextStage) {
                    
                    // check last enemy and all players die at the same time.
                    if (currentStage.canScrollStageHorizontally() 
                                                    && !LOCKS.isEmpty()) {
                        
                        currentLock = null;
                        Go.show(3, 700);
                    }
                    
                    if (!stageCleared && LOCKS.isEmpty()) {
                        stageCleared = true;
                    }
                }
                else if (nextStageTime >= 0 && !nextStageCalled
                        && Util.getTime() >= nextStageTime) {
                    
                    // check last enemy and all players die at the same time.
                    if (currentStage.canGoNextStage()) {
                        stageCleared = true;
                        nextStageCalled = true;
                        currentStage.goNext();
                    }
                }
                else if (nextStageTime < 0) {
                    nextStageTime = 
                            Util.getTime() + currentLock.nextStageWaitTime;
                }
            }
        }
        else {
            LockInfo lock = LOCKS.isEmpty() ? null : LOCKS.get(0);
            if (lock != null) {
                currentLock = lock;
                LOCKS.remove(0);
            }
        }
    }
    
    private static void updateFollowPlayers() {
        boolean followCameraPlayer1 = player1 != null 
                    && player1.isAlive();
        
        boolean followCameraPlayer2 = player2 != null 
                    && player2.isAlive();

        if (!followCameraPlayer1 && !followCameraPlayer2) {
            return;
        }
        double targetX 
                = Math.max(getPlayer1X(0), getPlayer2X(0)) - CANVAS_WIDTH / 2;

        double playerMaxX = Math.min(getPlayer1X(Double.MAX_VALUE)
                        , getPlayer2X(Double.MAX_VALUE)) - PLAYER_HALF_WIDTH;
        
        if (targetX > playerMaxX) {
            targetX = playerMaxX;
        }
        if (targetX > Terrain.getMaxX() - CANVAS_WIDTH) {
            targetX = Terrain.getMaxX() - CANVAS_WIDTH;
        }
        
        // scroll x until lock position automatically if autoScroll activated
        boolean scrollUntilLock = currentLock != null && currentLock.autoScroll;

        if (scrollUntilLock) {
            forcePlayersToMoveUntilLockPosition(
                    followCameraPlayer1, followCameraPlayer2);
        }
        
        double scrollLockSpeed = 0.0;
        if (followCameraPlayer1) {
            scrollLockSpeed = 20.0;
            scrollUntilLock &= currentLock != null && (int) player1.getLastValidWx()
                    >= (int) (currentLock.x + player1.getCollisionMargin() 
                        - currentLock.playersMoveMargin);
        }
        if (followCameraPlayer2) {
            scrollLockSpeed = 20.0;
            scrollUntilLock &= currentLock != null && (int) player2.getLastValidWx()
                    >= (int) (currentLock.x + player2.getCollisionMargin()
                        - currentLock.playersMoveMargin);
        }
        if (scrollUntilLock && currentLock.x > targetMinX) {
            //targetMinX += 0.5 * (currentLock.x - targetMinX);
            if (currentLock.x - targetMinX >= scrollLockSpeed) {
                targetMinX += scrollLockSpeed;
            } 
            else {
                targetMinX = currentLock.x;
            }
        }
        
        if (targetX < targetMinX) {
            targetX = targetMinX;
        }
        boolean followCameraYPlayer1 = player1 != null 
                && Terrain.followCameraY(player1.getWx(), player1.getWz()) 
                    || !followCameraPlayer1;
        
        boolean followCameraYPlayer2 = player2 != null
                && Terrain.followCameraY(player2.getWx(), player2.getWz()) 
                    || !followCameraPlayer2;
        
        double p1h = getPlayer1Z(0) + getPlayer1FloorHeight(0);
        double p2h = getPlayer2Z(0) + getPlayer2FloorHeight(0);
        if (!followCameraPlayer1) p1h = p2h;
        if (!followCameraPlayer2) p2h = p1h;

        if (targetX > Terrain.getWidth() - CANVAS_WIDTH) {
            targetX = Terrain.getWidth() - CANVAS_WIDTH;
        }
        if (currentLock != null && targetX > currentLock.x) {
            targetX = currentLock.x;
        }
        if (targetX > maxX) {
            targetX = maxX;
        }

        int targetMinY = getMinY(
                (int) (targetX + CANVAS_WIDTH));
        
        int targetMaxY = getMaxY(
                (int) (targetX + CANVAS_WIDTH)) - CANVAS_HEIGHT;

        double playerMaxY = Math.min(p1h, p2h) - PLAYER_HEIGHT;
        double targetY = Math.max(p1h, p2h) - CANVAS_HEIGHT * 0.75;
        double minMaxY = Math.min(playerMaxY, targetMaxY);
        if (targetY < 0) {
            targetY = 0;
        }
        if (targetY < targetMinY) {
            targetY = targetMinY;
        }
        if (targetY > minMaxY) {
            targetY = minMaxY;
        }
        
        //System.out.printf("camera targetY = %d \n", (int) targetY);
        
        if (hardShakeEnabled) {
            targetX += 32 * Math.sin(System.nanoTime() * 0.00000005);
        }
        else if (smoothShakeEnabled) {
            targetY += 6 * Math.sin(System.nanoTime() * 0.0000000025) - 6;
        }
        
        // lock the horizontal scroll if one of the players 
        // is deciding if he will continue the game when he dies.
        if ((GoldenAxeGame.player1 == null 
                || !GoldenAxeGame.player1.needsToAskContinue())
                    && (GoldenAxeGame.player2 == null 
                        || !GoldenAxeGame.player2.needsToAskContinue())) {
            
            x = x + (targetX - x) * FOLLOW_SPEED;
        }
        if (followCameraYPlayer1 && followCameraYPlayer2) {
            y += FOLLOW_SPEED * (targetY - y);
        }
        targetMinX = x;
        
        if ((int) x == sameLocationX) {
            sameLocationCounter++;
            // if 10 seconds in the same location, show go
            if (sameLocationCounter > 1200 && activeEnemiesCount == 0
                    && currentLock != null 
                        && x < currentLock.x - CANVAS_WIDTH / 2) {
                
                Go.show(4, 0);
                sameLocationCounter = 0;
            }
        }
        else {
            sameLocationX = (int) x;
            sameLocationCounter = 0;
        }
    }

    // force players to move util lock position if requested
    private static void forcePlayersToMoveUntilLockPosition(
                boolean followCameraPlayer1, boolean followCameraPlayer2) {
        
        double player1TargetX = 
                currentLock.x + (followCameraPlayer1 
                    ? 2 * player1.getCollisionMargin() : 0);

        double player2TargetX = 
                currentLock.x + (followCameraPlayer2 
                    ? 2 * player2.getCollisionMargin() : 0);
        
        if (!currentLock.playersMoveForced) {
            boolean playersStateOk = 
                    (!followCameraPlayer1 || player1.isActive())
                        && (!followCameraPlayer2 || player2.isActive());
            
            if (!playersStateOk) {
                return;
            }

                    
            if ((followCameraPlayer1 && player1.isThrowingOrThrown())
                || (followCameraPlayer2 && player2.isThrowingOrThrown())) {

                // just ignore while player is throwing or thrown
                //System.out.println("ignoring throw thrown");
            } 
            else if ((followCameraPlayer1 && (int) player1.getLastValidWx() >= 
                (int) (player1TargetX - player1.getCollisionMargin() 
                - currentLock.playersMoveMargin)) 
                    || (followCameraPlayer2 && (int) player2.getLastValidWx() >= 
                        (int) (player2TargetX - player2.getCollisionMargin() 
                        - currentLock.playersMoveMargin))) {

                if (followCameraPlayer1 
                        && (player1.isActive()) // && !player1.isJumping())
                            && ((followCameraPlayer1 ^ followCameraPlayer2) 
                                || player1.getLastValidWx() < player1TargetX)) {

                    if (player2 == null || !player2.needsToAskContinue()) {
                        if (player1.getLastValidWx() < player1TargetX) {
                            player1.walkTo(player1TargetX
                                    , player1.getLastValidWz(), "walking", null);
                        }
                        player1.setControlEnabled(false);
                        if (followCameraPlayer2) {
                            player2.setControlEnabled(false);
                        }
                        currentLock.playersMoveForced = true;
                    }
                }
                if (followCameraPlayer2 
                    && (player2.isActive()) // && !player2.isJumping())
                        && ((followCameraPlayer1 ^ followCameraPlayer2)
                            || player2.getLastValidWx() < player2TargetX)) {

                    if (player1 == null || !player1.needsToAskContinue()) {
                        if (player2.getLastValidWx() < player2TargetX) {
                            player2.walkTo(player2TargetX
                                    , player2.getLastValidWz(), "walking", null);
                        }
                        player2.setControlEnabled(false);
                        if (followCameraPlayer1) {
                            player1.setControlEnabled(false);
                        }
                        currentLock.playersMoveForced = true;
                    }
                }
                if ((followCameraPlayer1 && !player1.needsToAskContinue()
                    && player1.getLastValidWx() >= player1TargetX) 
                        && (followCameraPlayer2 && !player2.needsToAskContinue()
                            && player2.getLastValidWx() >= player2TargetX)) {                    
                    
                    player1.setControlEnabled(false);
                    player2.setControlEnabled(false);
                    currentLock.playersMoveForced = true;
                }
            }

            
        }
        else if (!currentLock.playersControlsEnabled && x < currentLock.x - 1.0) {
            
            if (followCameraPlayer1 && !player1.isControlEnabled() 
                && player1.isOnFloor() && player1.getLastValidWx() 
                    >= player1TargetX - 1.0 && player1.getStateManager()
                        .getCurrentState().getName().equals("walk_to")) {
                
                player1.cancelWalkTo();
            }
            if (followCameraPlayer2 && !player2.isControlEnabled() 
                && player2.isOnFloor() && player2.getLastValidWx() 
                    >= player2TargetX - 1.0 && player2.getStateManager()
                        .getCurrentState().getName().equals("walk_to")) {
                
                player2.cancelWalkTo();
            }
        }        
        else if (!currentLock.playersControlsEnabled 
                                            && x >= currentLock.x - 1.0) {
            
            if (followCameraPlayer1 && !player1.isControlEnabled() 
                && player1.getLastValidWx() 
                    >= player1TargetX - player1.getCollisionMargin() - 1.0) {
                
                player1.setControlEnabled(true);
                player1.cancelWalkTo();
            }
            if (followCameraPlayer2 && !player2.isControlEnabled() 
                && player2.getLastValidWx() 
                    >= player2TargetX - player2.getCollisionMargin() - 1.0) {
                
                player2.setControlEnabled(true);
                player2.cancelWalkTo();
            }
            
            if ((!followCameraPlayer1 || player1.isControlEnabled())
                    && (!followCameraPlayer2 || player2.isControlEnabled())) {
                
                currentLock.playersControlsEnabled = true;
            }
        }
    }
    
}
