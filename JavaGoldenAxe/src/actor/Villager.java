package actor;

import infra.Actor;
import infra.Audio;
import static infra.Direction.*;
import infra.Util;
import java.util.UUID;
import scene.Stage;

/**
 * Villager class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Villager extends Actor {
    
    public static enum Gender { MALE, FEMALE }
    
    // ensure only single sound is played for all villagers
    private static long nextSoundTime;
    
    private Gender gender;
    private double speedX;
    private double speedZ;
    private boolean onlyHorizontal;
    
    public Villager(Stage stage, String[] data) {
        super(stage, "villager_" + UUID.randomUUID()
                                    , 0, 0, "villagers", null, null);
        
        spawnInternal(data);
    }
    
    @Override
    public void spawn(String[] data) {
        super.spawn(data);
    }
    
    private void spawnInternal(String[] data) {
        // wx wz POTION|FOOD item_count
        wx = Integer.parseInt(data[4]);
        wz = Integer.parseInt(data[5]);
        gender = Gender.valueOf(data[6]);
        onlyHorizontal = Boolean.parseBoolean(data[7]);
        destroyIfNotAlive = true;
        speedX = 1.0 + 0.75 * Math.random();
        if (onlyHorizontal) {
            speedZ = 0;
        }
        else {
            speedZ = -0.15 + 0.3 * Math.random();
        }
        control = Control.NONE;
        //limitHorizontalMovementByCamera = false;
        stateManager.addState(new Running());    
        stateManager.switchTo("running");
    }

    
    // --- states ---
    
    private class Running extends ActorState {
        
        public Running() {
            super("running");
        }

        @Override
        public void onEnter() {
            running = true;
            String genderStr = gender.toString().toLowerCase();
            animationPlayer.setAnimation("running_" + genderStr);
            animationPlayer.setCurrentAnimationSpeed(speedX * 0.1);
            animationPlayer.setCurrentAnimationLooping(true);
            animationPlayer.setCurrentAnimationFrameIndex(4 * Math.random());
        }
        
        @Override
        public void fixedUpdate() {
            vx = -speedX;
            vz = speedZ;
            direction = vx < 0.0 ? LEFT : RIGHT;
            updateMovement();
            destroyIfOutOfViewLeft();
            animationPlayer.update();
            if (getTime() >= nextSoundTime) {
                Audio.playSound("scream");
                nextSoundTime = getTime() + 3000 + Util.random(3000);
            }
        }

    }
    
}
