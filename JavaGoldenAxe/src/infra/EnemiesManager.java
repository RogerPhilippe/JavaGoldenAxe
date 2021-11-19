package infra;

import actor.Enemy;
import actor.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import scene.Stage;

/**
 * EnemiesManager class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class EnemiesManager {
    
    private static Stage stageScene;
    private static final List<Integer> SPOT_ORDER_2 = new ArrayList<>();
    private static final List<Integer> SPOT_ORDER_4 = new ArrayList<>();
    
    static {
        SPOT_ORDER_2.addAll(Arrays.asList(new Integer[] { 0, 1 }));
        SPOT_ORDER_4.addAll(Arrays.asList(new Integer[] { 0, 1, 2, 3 }));
    }
    
    public static void reset(Stage stageScene) {
        EnemiesManager.stageScene = stageScene;
    }

    public static void update() {
        updateEnemiesStrategy();
    }
    
    private static void freeNotActivePlayersEnemy() {
        if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1.leftEnemy != null
                    && !GoldenAxeGame.player1.leftEnemy.isActive()) {
            
            freePlayersAttackSpot(GoldenAxeGame.player1.leftEnemy);
        }
        if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1.rightEnemy != null
                    && !GoldenAxeGame.player1.rightEnemy.isActive()) {
            
            freePlayersAttackSpot(GoldenAxeGame.player1.rightEnemy);
        }
        if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2.leftEnemy != null
                    && !GoldenAxeGame.player2.leftEnemy.isActive()) {
            
            freePlayersAttackSpot(GoldenAxeGame.player2.leftEnemy);
        }
        if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2.rightEnemy != null
                    && !GoldenAxeGame.player2.rightEnemy.isActive()) {
            
            freePlayersAttackSpot(GoldenAxeGame.player2.rightEnemy);
        }
    }
    
    private static void updateEnemiesStrategy() {
        freeNotActivePlayersEnemy();
        
        Collections.shuffle(stageScene.getActors());
        for (Actor actor : stageScene.getActors()) {
            if (actor instanceof Enemy enemy) {
                //System.out.println("enemyid = " + enemy.getId());
                if (!enemy.isActive()) {
                    freePlayersAttackSpot(enemy);
                    continue;
                }
                
                Player targetPlayer = getTargetPlayer(enemy);
                if (targetPlayer != null) {
                    // if player is not active remove the attacking enemies
                    if (!targetPlayer.isActive() && Util.random(100) < 5) {
                        freePlayersAttackSpot(targetPlayer.leftEnemy);
                        freePlayersAttackSpot(targetPlayer.rightEnemy);
                    }
                    else {
                        changeTargetPlayerIfOtherIsCloser(targetPlayer, enemy);
                    }
                    continue;
                }        

                if (!replaceCurrentPlayersEnemyIfOtherIsCloser(
                        GoldenAxeGame.player1, enemy)) {
                    
                    replaceCurrentPlayersEnemyIfOtherIsCloser(
                            GoldenAxeGame.player2, enemy);
                }
                
                Collections.shuffle(SPOT_ORDER_4);
                for (int i = 0; i < 4; i++) {
                    if (SPOT_ORDER_4.get(0) == i 
                        && GoldenAxeGame.player1 != null 
                            && GoldenAxeGame.player1.isActive() 
                                && GoldenAxeGame.player1.leftEnemy == null
                                && GoldenAxeGame.player1.rightEnemy != enemy) {

                        GoldenAxeGame.player1.leftEnemy = enemy;
                        break;
                    }
                    else if (SPOT_ORDER_4.get(1) == i 
                        && GoldenAxeGame.player1 != null 
                            && GoldenAxeGame.player1.isActive() 
                                && GoldenAxeGame.player1.rightEnemy == null
                                && GoldenAxeGame.player1.leftEnemy != enemy) {

                        GoldenAxeGame.player1.rightEnemy = enemy;
                        break;
                    }
                    else if (SPOT_ORDER_4.get(2) == i 
                        && GoldenAxeGame.player2 != null 
                            && GoldenAxeGame.player2.isActive() 
                                && GoldenAxeGame.player2.leftEnemy == null
                                && GoldenAxeGame.player2.rightEnemy != enemy) {

                        GoldenAxeGame.player2.leftEnemy = enemy;
                        break;
                    }
                    else if (SPOT_ORDER_4.get(3) == i 
                        && GoldenAxeGame.player2 != null 
                            && GoldenAxeGame.player2.isActive() 
                                && GoldenAxeGame.player2.rightEnemy == null
                                && GoldenAxeGame.player2.leftEnemy != enemy) {

                        GoldenAxeGame.player2.rightEnemy = enemy;
                        break;
                    }
                }
            }
        }
    }

    // return true if enemy replaced
    private static boolean replaceCurrentPlayersEnemyIfOtherIsCloser(
                                            Player player, Enemy otherEnemy) {
        
        boolean replaced = false;
        Collections.sort(SPOT_ORDER_2);
        for (int i = 0; i < 2; i++) {
            
            if (SPOT_ORDER_2.get(0) == i 
                    && player != null && player.isActive()
                        && player.leftEnemy != null 
                            && player.leftEnemy != otherEnemy 
                                && player.rightEnemy != otherEnemy) {

                double currentEnemyDistance = 
                            player.getSquaredDistance(otherEnemy);
                
                double leftEnemyDistance = 
                            player.getSquaredDistance(player.leftEnemy);

                double dx = Math.abs(leftEnemyDistance - currentEnemyDistance);

                if (currentEnemyDistance < leftEnemyDistance 
                            && dx > 1024.0) { 

                    freePlayersAttackSpot(otherEnemy);
                    player.leftEnemy = otherEnemy;
                    replaced = true;
                    break;
                }
            }
            else if (SPOT_ORDER_2.get(1) == i 
                    && player != null && player.isActive()
                        && player.rightEnemy != null 
                            && player.rightEnemy != otherEnemy
                                && player.leftEnemy != otherEnemy) {

                double currentEnemyDistance = 
                            player.getSquaredDistance(otherEnemy);
                
                double rightEnemyDistance = 
                            player.getSquaredDistance(player.rightEnemy);
            
                double dx = Math.abs(rightEnemyDistance - currentEnemyDistance);

                if (currentEnemyDistance < rightEnemyDistance 
                            && dx > 1024.0) { 

                    freePlayersAttackSpot(otherEnemy);
                    player.rightEnemy = otherEnemy;
                    replaced = true;
                    break;
                }
            }
        }
        return replaced;
    }

    // try to change enemy's target player if another player is closer
    private static void changeTargetPlayerIfOtherIsCloser(
                                        Player targetPlayer, Enemy enemy) {
        
        Player otherPlayer = getOtherTargetPlayer(targetPlayer);
        if (otherPlayer != null && otherPlayer.isActive()) {
            double currentTargetDistance = 
                    targetPlayer.getSquaredDistance(enemy);

            double otherTargetDistance = 
                    otherPlayer.getSquaredDistance(enemy);

            double dx = Math.abs(otherTargetDistance - currentTargetDistance);

            if (otherTargetDistance < currentTargetDistance && dx > 1024.0) {
                Collections.sort(SPOT_ORDER_2);
                for (int i = 0; i < 2; i++) {
                    if (SPOT_ORDER_2.get(0) == i 
                            && otherPlayer.leftEnemy == null
                                && otherPlayer.rightEnemy != enemy) {
                        
                        freePlayersAttackSpot(enemy);
                        otherPlayer.leftEnemy = enemy;
                        break;
                    }
                    else if (SPOT_ORDER_2.get(1) == i 
                            && otherPlayer.rightEnemy == null
                                && otherPlayer.leftEnemy != enemy) {
                        
                        freePlayersAttackSpot(enemy);
                        otherPlayer.rightEnemy = enemy;
                        break;
                    }
                }
            }
        }        
    }
    
    public static Player getOtherTargetPlayer(Player thisPlayer) {
        if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1 == thisPlayer) {
            
            return GoldenAxeGame.player2;
        }
        if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2 == thisPlayer) {
            
            return GoldenAxeGame.player1;
        }
        return null;
    }
    
    public static Player getTargetPlayer(Enemy enemy) {
        Player targetPlayer = null;
        if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1.leftEnemy == enemy) {
            
            targetPlayer = GoldenAxeGame.player1;
        }
        if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1.rightEnemy == enemy) {
            
            targetPlayer = GoldenAxeGame.player1;
        }
        if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2.leftEnemy == enemy) {
            
            targetPlayer = GoldenAxeGame.player2;
        }
        if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2.rightEnemy == enemy) {
            
            targetPlayer = GoldenAxeGame.player2;
        }        
        return targetPlayer;
    }
    
    public static void freePlayersAttackSpot(Enemy enemy) {
        if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1.leftEnemy == enemy) {
            
            GoldenAxeGame.player1.leftEnemy = null;
        }
        if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1.rightEnemy == enemy) {
            
            GoldenAxeGame.player1.rightEnemy = null;
        }
        if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2.leftEnemy == enemy) {
            
            GoldenAxeGame.player2.leftEnemy = null;
        }
        if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2.rightEnemy == enemy) {
            
            GoldenAxeGame.player2.rightEnemy = null;
        }
    }
    
    public static Player getFirstValidPlayer() {
        if (GoldenAxeGame.player1 != null 
                && !GoldenAxeGame.player1.isGameOver()) {
            
            return GoldenAxeGame.player1;
        }
        if (GoldenAxeGame.player2 != null 
                && !GoldenAxeGame.player2.isGameOver()) {
            
            return GoldenAxeGame.player2;
        }
        return null;
    }
    
}
