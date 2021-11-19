 package actor;

import infra.Actor;
import static infra.Actor.Control.*;
import infra.Animation;
import infra.Camera;
import infra.Collider;
import infra.Collider.Reaction;
import static infra.Collider.Reaction.*;
import static infra.Direction.*;
import infra.EnemiesManager;
import infra.GoldenAxeGame;
import infra.Input;
import static infra.Settings.*;
import infra.Terrain;
import infra.Util;
import java.awt.event.KeyEvent;
import scene.Stage;

/**
 * Enemy class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Enemy extends Actor {
    
    public Enemy(Stage stage, String enemyId, String[] data) {
        super(stage, data[5], 0, 0, enemyId, data[4], null);
        control = Control.IA;
        destroyIfDeathHeight = true;
        spawnInternal(data);
        if (animationPlayer.isParameterAvailable("mountable")) {
            mountable = Boolean.parseBoolean(
                    animationPlayer.getParameter("mountable"));
        }
        if (animationPlayer.isParameterAvailable(subtype + "_boss")) {
            boss = Boolean.parseBoolean(
                    animationPlayer.getParameter(subtype + "_boss"));
        }
        else {
            boss = false;
        }
        if (boss) {
            horizontalCameraLimitMarginX = 8;
        }
        if (animationPlayer.isParameterAvailable(subtype + "_max_energy")) {
            energy = Integer.parseInt(
                    animationPlayer.getParameter(subtype + "_max_energy"));
        }
    }
    
    @Override
    protected boolean isHorizontalMovementByCameraLimited() {
        return boss || super.isHorizontalMovementByCameraLimited();
    }
    
    @Override
    public void spawn(String[] data) {
        if (!isStarted()) {
            start();
            setStarted(true);
        }
        spawnInternal(data);
    }
    
    private void spawnInternal(String[] data) {
        // entity_subtype entity_id locking_camera_id wx wz initial_state
        lockingCameraId = data[6];
        if (data[6] == null || data[6].equals("null") 
                                    || data[6].equals("empty")) {
            
            lockingCameraId = null;    
        }
        
        wx = Integer.parseInt(data[7]);
        wz = Integer.parseInt(data[8]);
        direction = LEFT;
        ActorState actorState = (ActorState) stateManager.getState(data[9]);
        if (actorState != null) {
            actorState.spawn(data);
        }
        stateManager.switchTo(data[9]);
        
        if (animationPlayer != null) {
            if (animationPlayer.isParameterAvailable("control")) {
                control = Control.valueOf(
                        animationPlayer.getParameter("control"));
            }
            if (animationPlayer.isParameterAvailable("walk_speed")) {
                walkSpeed = Double.parseDouble(
                        animationPlayer.getParameter("walk_speed"));
            }
            if (animationPlayer.isParameterAvailable(subtype + "_walk_speed")) {
                walkSpeed = Double.parseDouble(
                        animationPlayer.getParameter(subtype + "_walk_speed"));
            }
            if (animationPlayer.isParameterAvailable(subtype + "_run_speed")) {
                runSpeed = Double.parseDouble(
                        animationPlayer.getParameter(subtype + "_run_speed"));
            }
            if (animationPlayer.isParameterAvailable(
                    subtype + "_attack_distance_min_x")) {
                
                attackDistanceMinX = Integer.parseInt(
                        animationPlayer.getParameter(
                                subtype + "_attack_distance_min_x"));
            }
            if (animationPlayer.isParameterAvailable(
                    subtype + "_attack_short_distance_min_x")) {
                
                attackShortDistanceMinX = Integer.parseInt(
                        animationPlayer.getParameter(
                                subtype + "_attack_short_distance_min_x"));
            }
            if (animationPlayer.isParameterAvailable(
                    subtype + "_attack_frequency")) {
                
                attackFrequency = Integer.parseInt(
                        animationPlayer.getParameter(
                                subtype + "_attack_frequency"));
            }
            if (animationPlayer.isParameterAvailable(
                    subtype + "_jump_attack_distance_x")) {
                
                jumpAttackDistanceX = Integer.parseInt(
                        animationPlayer.getParameter(
                                subtype + "_jump_attack_distance_x"));
            }
            if (animationPlayer.isParameterAvailable(
                    subtype + "_dif_z_attack_distance")) {
                
                difZAttackDistance = Integer.parseInt(
                        animationPlayer.getParameter(
                                subtype + "_dif_z_attack_distance"));
            }
            if (animationPlayer.isParameterAvailable(
                    subtype + "_dif_y_attack_distance")) {
                
                difYAttackDistance = Integer.parseInt(
                        animationPlayer.getParameter(
                                subtype + "_dif_y_attack_distance"));
            }
        }
    }

    private int firstAttacksCount;
    private long attack1or2Time = Long.MAX_VALUE;

    @Override
    public void react(
            Reaction reaction, Collider sourceCollider, Actor sourceActor) {
        
        reaction = knockDownIfHitByPlayerUsingOnlyMultipleFirstAttacks(
                                                        reaction, sourceActor);

        super.react(reaction, sourceCollider, sourceActor);

        if (boss && energy == 0) {
            if (wx < Camera.getX() + 32) {
                direction = LEFT;
            }
            else if (wx > Camera.getX() + CANVAS_WIDTH - 32) {
                direction = RIGHT;
            }
            horizontalCameraLimitMarginX = 16;
            //System.out.println("boss is dead! direction=" + direction);
        }
    }
    
    // if hit multiple times by player's first attacks, this enemy knocks down
    private Reaction knockDownIfHitByPlayerUsingOnlyMultipleFirstAttacks(
                                        Reaction reaction, Actor sourceActor) {
        String attackName = 
                sourceActor.getStateManager().getCurrentState().getName();
        
        if ((attackName.equals("attack_1") || attackName.equals("attack_2")) 
                && Util.getTime() - attack1or2Time < 3000) {
            
            firstAttacksCount++;
            attack1or2Time = Util.getTime();
        }
        else {
            firstAttacksCount = 0;
            attack1or2Time = Long.MAX_VALUE;
        }
        
        
        if (firstAttacksCount >= 3 && Util.random(10) > 2) {
            reaction = KNOCK_DOWN;
        }
        
        if (reaction == KNOCK_DOWN) {
            firstAttacksCount = 0;
            attack1or2Time = Long.MAX_VALUE;
        }
        //System.out.println(
        //        "attack name: " + attackName + " count " + firstAttacksCount);
        return reaction;
    }
    
    private int attackDistanceMinX = 32;
    private int attackShortDistanceMinX = 16;
    private int attackFrequency = 2;
    private int jumpAttackDistanceX = 48;
    private int difZAttackDistance = 24;
    private int difYAttackDistance = 32;
    private int projectileMinDistanceX = 96;
            
    public boolean isActive() {
        String currentState = stateManager.getCurrentState().getName();
        return isAlive() && control == IA 
            && !currentState.equals("none") 
                && !currentState.equals("mouting") 
                    && !currentState.equals("mounted") 
                        && !currentState.equals("resting") 
                            && !currentState.equals("wait_until_actors_death")
                                && !currentState.equals("stand_by");
    }
    
    @Override
    public void updateControlIA() {
        String currentState = stateManager.getCurrentState().getName();
        if (currentState.equals("walk_to")) {
            return;
        }
        
        Player targetPlayer = EnemiesManager.getTargetPlayer(this);
        if (targetPlayer != null) {
            updateControlIAInternal(targetPlayer);
        }
        else {
            updateRandomWalk();
        }
    }
    
    private double targetX = -1;
    private double targetZ = -1;
    private long walkWaitTime;
    
    private void updateRandomWalk() {
        if (Util.getTime() < walkWaitTime) {
            return;
        }
        
        double difX = targetX - wx;
        double difZ = targetZ - wz;
        
        if (targetX < 0 || (Math.abs(difX) < 2 * walkSpeed 
                                && Math.abs(difZ) < 2 * walkSpeed)) {
            
            do {
                targetX = Camera.getX() + 32 + Util.random(CANVAS_WIDTH - 64);
                targetZ = wz + 32 * Math.random() - 16;
            } 
            while (!Terrain.isWalkable(targetX, targetZ) 
                || Terrain.getHeight(targetX, targetZ) >= Terrain.MAX_HEIGHT);
            
            walkWaitTime = Util.getTime() + 1000 + Util.random(1000);
            
            // select a direction for the wait state
            direction = wx >= Camera.getX() + CANVAS_WIDTH / 2 ? LEFT : RIGHT;
            Player firstPlayer = EnemiesManager.getFirstValidPlayer();
            if (firstPlayer != null) {
                double dx = firstPlayer.getWx() - wx;
                direction = dx > 0 ? RIGHT : LEFT;
            }
            else {
                double dx = (Camera.getX() + CANVAS_WIDTH / 2) - wx;
                direction = dx > 0 ? RIGHT : LEFT;
            }
            return;
        }
        
        if (difX > 2 * walkSpeed 
                && Terrain.getHeight(wx + collisionMargin + 1.0, wz) 
                                            < Terrain.getDeathHeight()) {
            
            walkRightControlEnabled = true;
            direction = RIGHT;
            int nextLocationHeight = 
                    Terrain.getHeight(wx + collisionMargin + 1.0, wz);
            
            double heightDif = Math.abs(floorHeight - nextLocationHeight);
            if (heightDif < 128 && nextLocationHeight < floorHeight && 
                ((heightDif <= 48.0 && Util.random(120) < 2) 
                    || (heightDif > 48.0 && Util.random(120) < 1))) {

                jumpControlEnabled = true;
            }
        }
        else if (difX < -2 * walkSpeed 
                && Terrain.getHeight(wx - collisionMargin - 1.0, wz) 
                                            < Terrain.getDeathHeight()) {
            
            walkLeftControlEnabled = true;
            direction = LEFT;
            int nextLocationHeight = 
                    Terrain.getHeight(wx - collisionMargin - 1.0, wz);
            
            double heightDif = Math.abs(floorHeight - nextLocationHeight);
            if (heightDif < 128 && nextLocationHeight < floorHeight && 
                ((heightDif <= 48.0 && Util.random(120) < 2) 
                    || (heightDif > 48.0 && Util.random(120) < 1))) {

                jumpControlEnabled = true;
            }
        }
        if (difZ > 2 * walkSpeed 
                && Terrain.getHeight(wx, wz + collisionMargin + 1.0) 
                                            < Terrain.getDeathHeight()) {
            
            downControlEnabled = true;
        }
        else if (difZ < -2 * walkSpeed 
                && Terrain.getHeight(wx, wz - collisionMargin - 1.0) 
                                            < Terrain.getDeathHeight()) {
            
            upControlEnabled = true;
        }        
    }
    
    final long iaTargetZAddStartRandom = Util.random(9999);
    
    private void updateControlIAInternal(Player targetPlayer) {
         // reset random walk
        targetX = -1;
        walkWaitTime = 0;
        
        int playerDifX = (int) (wx - targetPlayer.getWx());
        direction = playerDifX < -walkSpeed 
                ? RIGHT : playerDifX > walkSpeed ? LEFT : direction;

        double dx = direction.getDx();
        if (targetPlayer.leftEnemy == this) dx = -1.0;
        if (targetPlayer.rightEnemy == this) dx = 1.0;
        
        int iaTargetX = (int) (targetPlayer.getWx() 
                                    + dx * attackDistanceMinX);
        
        int iaTargetZAdd = 0;
        if (direction.getDx() == dx && Math.abs(playerDifX) > 4) {
            if (((Util.getTime() / 1000) + iaTargetZAddStartRandom) % 20 < 10) {
                iaTargetZAdd = 32;
            }
            else {
                iaTargetZAdd = -32;
            }
        }
        
        int difX = (int) (wx - iaTargetX);
        int difY = (int) (wy - targetPlayer.getWy());
        int difZ = (int) (wz - targetPlayer.getWz());
        
        int walkDifZ = (int) (wz - targetPlayer.getWz() - iaTargetZAdd);
        
        // workaround to not get stuck on terrain
        boolean forceGoUp = false;
        for (int tx = 0; tx <= collisionMargin + walkSpeed; tx++) {
            if (Terrain.forceEnemyGoingUp(wx - Math.signum(difX) * tx, wz)) {
                forceGoUp = true;
                break;
            }
        }
        
        if (difX > 80 && Math.abs(difZ) < difZAttackDistance 
                                && !running && Util.random(100) < 2) {
            
            runLeftControlEnabled = true;
            walkLeftControlEnabled = true;
        }
        else if (difX < -80 && Math.abs(difZ) < difZAttackDistance 
                                && !running && Util.random(100) < 2) {
            
            runRightControlEnabled = true;
            walkRightControlEnabled = true;
        }
        else if (targetPlayer.isHittable()
                && Math.abs(playerDifX) <= attackDistanceMinX + 1.0 
                    && Math.abs(difY) < difYAttackDistance 
                        && Math.abs(difZ) < difZAttackDistance
                            && Util.random(100) < attackFrequency) {
            
            if (Math.abs(playerDifX) < attackShortDistanceMinX 
                    && stateManager.isStateAvailable("attack_short")) {
                
                fire2ControlEnabled = true;
            }
            else {
                fireControlEnabled = true;
            }
        }
        else {
            if (difX < 0 
                    && Terrain.getHeight(wx + collisionMargin + 1.0, wz) 
                                                < Terrain.getDeathHeight()) {
                
                walkRightControlEnabled = true;
                
                int nextLocationHeight = 
                        Terrain.getHeight(wx + collisionMargin + 1.0, wz);

                double heightDif = Math.abs(floorHeight - nextLocationHeight);
                if (heightDif < 128 && nextLocationHeight < floorHeight && 
                    ((heightDif <= 48.0 && Util.random(120) < 2) 
                        || (heightDif > 48.0 && Util.random(120) < 1))) {

                    jumpControlEnabled = true;
                }
                else if (animationPlayer.isAnimationAvailable("jumping_attack")
                    && Util.random(100) < 2 
                        && targetPlayer.isHittable()
                            && Math.abs(playerDifX) <= 64
                                && Math.abs(difY) < difYAttackDistance 
                                    && Math.abs(difZ) < difZAttackDistance) {
                    
                    jumpControlEnabled = true;
                }
            }
            else if (difX > 0 
                    && Terrain.getHeight(wx - collisionMargin - 1.0, wz) 
                                                < Terrain.getDeathHeight()) {
                
                walkLeftControlEnabled = true;

                int nextLocationHeight = 
                        Terrain.getHeight(wx - collisionMargin - 1.0, wz);

                double heightDif = 
                        Math.abs(floorHeight - nextLocationHeight);
                
                if (heightDif < 128 && nextLocationHeight < floorHeight && 
                    ((heightDif <= 48.0 && Util.random(120) < 2) 
                        || (heightDif > 48.0 && Util.random(120) < 1))) {

                    jumpControlEnabled = true;
                }
                else if (animationPlayer.isAnimationAvailable("jumping_attack")
                    && Util.random(100) < 2 
                        && targetPlayer.isHittable()
                            && Math.abs(playerDifX) <= 64
                                && Math.abs(difY) < difYAttackDistance 
                                    && Math.abs(difZ) < difZAttackDistance) {
                    
                    jumpControlEnabled = true;
                }
            }
            if (!forceGoUp && walkDifZ < 0 
                    && Terrain.getHeight(wx, wz + collisionMargin + 1.0) 
                                                < Terrain.getDeathHeight()) {
                
                downControlEnabled = true;
            }
            else if (forceGoUp || (walkDifZ > 0 
                    && Terrain.getHeight(wx, wz - collisionMargin - 1.0) 
                                                < Terrain.getDeathHeight())) {
                
                upControlEnabled = true;
            }
        }
        
        // death stab or walk jump and attack
        if (targetPlayer.isHittable()
            && (animationPlayer.isAnimationAvailable("jumping_death_stab")
                || animationPlayer.isAnimationAvailable("jumping_attack"))
                    && Math.abs(difZ) < difZAttackDistance
                        && ((Math.abs(difY) > 34
                            && Math.abs(difY) < 37
                                && Util.random(3) > 0)) 
                                    || floorHeight - wy > 96) {
            
            fireControlEnabled = true;
        }
        
        // run and attack or run and jump if death stab is available
        if (targetPlayer.isHittable() && running 
                && Math.abs(difX) < jumpAttackDistanceX 
                    && Math.abs(difY) < difYAttackDistance 
                        && Math.abs(difZ) < difZAttackDistance / 2) {
            
            if (animationPlayer.isAnimationAvailable("jumping_death_stab")
                    && Util.random(2) < 1) {
                
                jumpControlEnabled = true;
            }
            else {
                fireControlEnabled = true;
            }
        }
        
        // try to throw projectile if this enemy is capable
        updateTryThrowProjectile();
        
        // if demo, disable attack controls
        if (GoldenAxeGame.showingDemo) {
            fireControlEnabled = false;
            fire2ControlEnabled = false;
            magicControlEnabled = false;
            projectileControlEnabled = false;
        }
    }
    
    private void updateTryThrowProjectile() {
        if (!stateManager.isStateAvailable("attack_projectile")) {
            return;
        }
        
        // enemy must be visible in the screen
        if (wx + collisionMargin < Camera.getX() 
                || wx - collisionMargin > Camera.getX() + CANVAS_WIDTH) {
            
            return;
        }
        
        boolean checkZ = true;
        
        Attack attack = (Attack) stateManager.getState("attack_projectile");
        Projectile projectile = attack.getProjectile();
        Animation animation = projectile.getAnimationPlayer()
                                .getAnimation(projectile.getId());
        
        if (animation.isParameterAvailable("try_to_follow_player_z")) {
            checkZ = !Boolean.parseBoolean(
                    animation.getParameter("try_to_follow_player_z"));
        }
        
        boolean fire = false;
        if (direction == RIGHT) {
            if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1.isHittable()
                    && GoldenAxeGame.player1.getWx() > wx
                    && (!checkZ || Math.abs(GoldenAxeGame.player1.getWz() - wz) 
                        < difZAttackDistance)
                            && Math.abs(wx - GoldenAxeGame.player1.getWx()) 
                                >= projectileMinDistanceX) {

                fire = true;
            }
            if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2.isHittable()
                    && GoldenAxeGame.player2.getWx() > wx
                    && (!checkZ || Math.abs(GoldenAxeGame.player2.getWz() - wz) 
                        < difZAttackDistance)
                            && Math.abs(wx - GoldenAxeGame.player2.getWx()) 
                                >= projectileMinDistanceX) {

                fire = true;
            }
        }
        else if (direction == LEFT) {
            if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1.isHittable()
                    && GoldenAxeGame.player1.getWx() < wx
                    && (!checkZ || Math.abs(GoldenAxeGame.player1.getWz() - wz) 
                        < difZAttackDistance)
                            && Math.abs(wx - GoldenAxeGame.player1.getWx()) 
                                >= projectileMinDistanceX) {

                fire = true;
            }
            if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2.isHittable()
                    && GoldenAxeGame.player2.getWx() < wx
                    && (!checkZ || Math.abs(GoldenAxeGame.player2.getWz() - wz) 
                        < difZAttackDistance)
                            && Math.abs(wx - GoldenAxeGame.player2.getWx()) 
                                >= projectileMinDistanceX) {

                fire = true;
            }
        }
        if (fire && Util.random(100) < 2) {
            projectileControlEnabled = true;
        }
    }
    
    @Override
    public void hit(int damage) {
        energy -= damage;
        if (isBoss() && Camera.getNotBossEnemiesCount() > 0
                && energy < MAX_ENERGY / 4) {
            
            energy = MAX_ENERGY / 4;
        }
        else if (energy < 0) {
            energy = 0;
        }
    }
    
    @Override
    public String toString() {
        return "Enemy{" + "id=" + id + '}';
    }
    
}
