package infra;

import actor.Decoration;
import actor.Enemy;
import actor.Thief;
import actor.Villager;
import actor.Water;
import static infra.Command.Type.*;
import java.util.UUID;
import scene.Stage;

/**
 * Command class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Command extends Actor {

    public static enum Type { 
        PLAY_MUSIC, 
        BACKGROUND_ENABLED, 
        BACKGROUND_TRANSITION, 
        BACKGROUND_SCROLL_VELOCITY_TRANSITION, 
        SHADOW_SHEAR_TRANSITION,
        CAMERA_SMOOTH_SHAKE_ENABLED,
        SPAWN,
        MOUNT,
        ADD_BLOCKER,
        REMOVE_BLOCKER,
        ANIMATOR
    }
    
    private final double triggerX;
    private final Type type;
    private final String[] parameters;
    private final Actor spawnActor;
    
    public Command(Stage stage, String[] parameters) {
        super(stage, "command_" + UUID.randomUUID(), 0, 0, null, null, null);
        collisionCheckEnabled = false;
        this.parameters = parameters;
        triggerX = Double.parseDouble(parameters[1]);
        type = Type.valueOf(parameters[2]);
        // spawn actor
        if (type == SPAWN) {
            spawnActor = getSpawnActor(parameters);
            if (spawnActor != null && spawnActor.isLockingCamera()) {
                Camera.addLockingActor(spawnActor);
            }
        }
        else {
            spawnActor = null;
        }
        // animator
        if (type == ANIMATOR) {
            stage.loadAnimator(parameters[3]);
        }
    }
    
    @Override
    public void fixedUpdate() {
        if (Camera.getX() >= triggerX) {
            switch (type) {
                case PLAY_MUSIC -> {
                    String musicId = parameters[3];
                    double speed = Double.parseDouble(parameters[4]);
                    long delayTime = Long.parseLong(parameters[5]);
                    Audio.playNextMusic(musicId, speed, delayTime);
                }
                case CAMERA_SMOOTH_SHAKE_ENABLED -> {
                    Camera.setSmoothShakeEnabled(
                            Boolean.valueOf(parameters[3]));
                }
                case BACKGROUND_ENABLED -> {
                    int layer = Integer.parseInt(parameters[3]);
                    boolean enabled = Boolean.parseBoolean(parameters[4]);
                    stage.setBackgroundEnabled(layer, enabled);
                }
                case BACKGROUND_TRANSITION -> {
                    int layer = Integer.parseInt(parameters[3]);
                    String transitionId = parameters[4];
                    double transitionSpeed = Double.parseDouble(parameters[5]);
                    long delayTime = Long.parseLong(parameters[6]);
                    stage.startBackgroundTransition(
                            layer, transitionId, transitionSpeed, delayTime);
                    
                }
                case BACKGROUND_SCROLL_VELOCITY_TRANSITION -> {
                    int layer = Integer.parseInt(parameters[3]);
                    String backgroundId = parameters[4];
                    double targetVx = Double.parseDouble(parameters[5]);
                    double targetVy = Double.parseDouble(parameters[6]);
                    double transitionSpeed = Double.parseDouble(parameters[7]);
                    stage.startBackgroundVelocityTransition(layer
                        , backgroundId, targetVx, targetVy, transitionSpeed);
                    
                }
                case SHADOW_SHEAR_TRANSITION -> {
                    double shearTarget = Double.parseDouble(parameters[3]);
                    double transitionSpeed = Double.parseDouble(parameters[4]);
                    stage.startShadowShearTransition(
                                shearTarget, transitionSpeed);
                }
                case MOUNT -> {
                    String beastId = parameters[3];
                    String actorId = parameters[4];
                    Actor beast = stage.getActorById(beastId);
                    Actor actor = stage.getActorById(actorId);
                    if (beast != null && actor != null) {
                        String beastStateName = 
                                beast.stateManager.getCurrentState().getName();

                        String actorStateName = 
                                actor.stateManager.getCurrentState().getName();
                        
                        if (!beastStateName.equals("none")
                            && !beastStateName.equals("wait_until_actors_death")
                                && !actorStateName.equals("none")
                                    && !actorStateName.equals(
                                        "wait_until_actors_death")) {
                        
                            beast.mount(actor);
                            actor.mountedActor = beast;
                            actor.stateManager.switchTo("mounting");                        
                        }
                        else {
                            return;
                        }
                    }
                    else {
                        return;
                    }
                }
                case ADD_BLOCKER -> {
                    String blockerId = parameters[3];
                    int blockerX = Integer.parseInt(parameters[4]);
                    int blockerZ = Integer.parseInt(parameters[5]);
                    int blockerWidth = Integer.parseInt(parameters[6]);
                    int blockerHeight = Integer.parseInt(parameters[7]);
                    Terrain.addBlocker(blockerId
                            , blockerX, blockerZ, blockerWidth, blockerHeight);
                }
                case REMOVE_BLOCKER -> {
                    String blockerId = parameters[3];
                    Terrain.removeBlocker(blockerId);
                }
                case SPAWN -> {
                    if (spawnActor != null) {
                        stage.addActor(spawnActor);
                        if (spawnActor instanceof Enemy enemy 
                                            && !enemy.isMountable()) {
                            
                            Camera.incActiveEnemyCount();
                        }
                    }        
                }
                case ANIMATOR -> {
                    stage.playAnimator(parameters[3], parameters[4]);
                }
                default -> {
                }
            }
            destroy();
        }
    }

    private Actor getSpawnActor(String[] parameters) {
        Actor actor = null;
        switch (parameters[3]) {
            case "player_1" -> {
                if (GoldenAxeGame.player1 != null 
                    && GoldenAxeGame.player1.isAlive()
                        && !GoldenAxeGame.player1.isDestroyed()) {

                    GoldenAxeGame.player1.spawn(parameters);
                    GoldenAxeGame.player1.reset();
                    stage.addActor(GoldenAxeGame.player1);
                    Camera.setPlayer1(GoldenAxeGame.player1);
                }
            }
            case "player_2" -> {
                if (GoldenAxeGame.player2 != null 
                    && GoldenAxeGame.player2.isAlive()
                        && !GoldenAxeGame.player2.isDestroyed()) {

                    GoldenAxeGame.player2.spawn(parameters);
                    GoldenAxeGame.player2.reset();
                    stage.addActor(GoldenAxeGame.player2);
                    Camera.setPlayer2(GoldenAxeGame.player2);
                }
            }
            case "golden_axe" ->  {
                    actor = new Actor(
                        stage, parameters[3], 0, 0, "golden_axe", null, null);
                    
                    actor.getStateManager().switchTo("golden_axe");
            }    
            case "chicken_leg", "dragon_blue" , "dragon_red" 
                    , "heninger" , "longmoan" , "storchinaya" 
                    , "skeleton" , "bad_brothers" , "lieutenant_bitter" 
                    , "death_adder"
                    
                        -> actor = new Enemy(stage, parameters[3], parameters);
            case "decoration" 
                    -> actor = new Decoration(stage, parameters);
            case "water" 
                    -> actor = new Water(stage);
            case "villager" 
                    -> actor = new Villager(stage, parameters);
            case "thief" -> {
                        String thiefId = "thief_" + UUID.randomUUID();
                        actor = new Thief(stage, thiefId, parameters);
                    }
            default -> { 
                throw new RuntimeException("invalid actor !");
                    }
        }
        return actor;
    }

}
