package actor;

import infra.Actor;
import infra.Animation;
import infra.Audio;
import infra.Direction;
import infra.Util;
import java.awt.Graphics2D;
import scene.Stage;

/**
 * Decoration class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Decoration extends Actor {
    
    private String animationId;
    private double animationFrameInc;
    private boolean animationLoop;
    private boolean drawingBeforeShadow;
    private long delayTime;
    private long startTime = -1;
    private boolean animationStarted;
    private boolean destroyAtEndOfAnimation;
    
    public Decoration(Stage stage, String[] data) {
        super(stage, data[4], 0, 0, "decorations", null, null);
        spawnInternal(data);
    }

    public boolean isDrawingBeforeShadow() {
        return drawingBeforeShadow;
    }

    @Override
    public void spawn(String[] data) {
        spawnInternal(data);
    }
    
    private void spawnInternal(String[] data) {
        // command 4830 SPAWN skeleton default skeleton_5_c wave_5 4910 330 
        //              wait_until_actors_death 1 longmoan_5 appearance 1000
        
        // command 4830 SPAWN decoration decoration_id hole_red 4910 -191 330 
        //          0.1 false true 1000 wait_until_actors_death 1 longmoan_5
        
        wx = Integer.parseInt(data[6]);
        wz = Integer.parseInt(data[7]);
        wy = Integer.parseInt(data[8]);
        jumpHeightInterpolationEnabled = false;
        jumpHeight = 0;
        animationId = data[5];
        animationFrameInc = Double.parseDouble(data[9]);
        animationLoop = Boolean.parseBoolean(data[10]);
        drawingBeforeShadow = Boolean.parseBoolean(data[11]);
        delayTime = Long.parseLong(data[12]);
        
        stateManager.addState(new Animating());    
        if (data.length > 13 && data[13].equals("wait_until_actors_death")) {
            System.arraycopy(data, 13, data, 9, data.length - 13);
            data[9 + (data.length - 13)] = "animating";
            ActorState actorState = (ActorState) 
                    stateManager.getState("wait_until_actors_death");
            
            actorState.spawn(data);
            stateManager.switchTo("wait_until_actors_death");
        }
        else {
            stateManager.switchTo("animating");
        }
    }
    
    @Override
    public void start() {
        checkDeathHeight = false;
        control = Control.NONE;
    }

   
    // --- states ---
    
    private class Animating extends ActorState {
        
        public Animating() {
            super("animating");
        }

        @Override
        public void onEnter() {
            animationPlayer.setAnimation(animationId);
            animationPlayer.setCurrentAnimationSpeed(animationFrameInc);
            animationPlayer.setCurrentAnimationLooping(animationLoop);
            
            Animation animation = animationPlayer.getCurrentAnimation();
            if (animation.isParameterAvailable("destroy_at_end_of_animation")) {
                destroyAtEndOfAnimation = Boolean.parseBoolean(
                        animation.getParameter("destroy_at_end_of_animation"));
            }
        }

        @Override
        public void fixedUpdate() {
            if (!animationStarted) {
                if (startTime < 0) {
                    startTime = Util.getTime() + delayTime;
                }
                else if (Util.getTime() >= startTime) {
                    animationStarted = true;
                    //stateManager.switchTo("animating");
                    // play associated sound
                    Animation animation = animationPlayer.getCurrentAnimation();
                    if (animation.isParameterAvailable("sound")) {
                        Audio.playSound(animation.getParameter("sound"));
                    }
                }
            }
            else {
                animationPlayer.update();
                if (destroyAtEndOfAnimation 
                    && animationPlayer.getCurrentAnimation().isFinished()) {
                    
                    destroy();
                }
                destroyIfOutOfViewLeft();
            }
        }

        @Override
        public void draw(Graphics2D g, double wx, double wy, double wz
                , int cameraX, int cameraY, Direction direction, boolean blink
                    , double scaleX, double scaleY, double angle) {

            if (animationStarted) {
                super.draw(g, wx, wy, wz, cameraX, cameraY
                            , direction, blink, scaleX, scaleY, angle);
            }
        }
        
    }
    
}
