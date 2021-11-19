package actor;

import infra.Actor;
import infra.Direction;
import java.util.UUID;
import scene.Stage;

/**
 * Dust class.
 * 
 * Dust effect when knock down player or enemy actors.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Dust extends Actor {
    
    private double animationSpeed;
    private double animationStartFrame;
    
    public Dust(Stage stage) {
        super(stage, "dust_" + UUID.randomUUID(), 0, 0, "effects", null, null);
    }

    public void spawn(Direction direction, double wx, double wy, double wz
                , double animationSpeed, double animationStartFrame) {
        
        if (!isStarted()) {
            start();
            setStarted(true);
        }
        this.direction = direction;
        this.wx = wx;
        this.wy = wy;
        this.wz = wz;
        this.animationSpeed = animationSpeed;
        this.animationStartFrame = animationStartFrame;
        stateManager.switchTo("animating");
        stage.addActor(this);
        destroyed = false;
    }
    
    @Override
    public void start() {
        control = Control.NONE;
        stateManager.addState(new None());    
        stateManager.addState(new Animating());    
        stateManager.switchTo("none");
    }

    // --- states ---
    
    private class Animating extends ActorState {
        
        public Animating() {
            super("animating");
        }

        @Override
        public void onEnter() {
            animationPlayer.setAnimation("dust");
            animationPlayer.setCurrentAnimationSpeed(animationSpeed);
            animationPlayer.setCurrentAnimationLooping(false);
            animationPlayer.setCurrentAnimationFrameIndex(animationStartFrame);
        }

        @Override
        public void fixedUpdate() {
            animationPlayer.update();
            double frameIndex = animationPlayer.getCurrentAnimationFrameIndex();
            if (frameIndex >= 6.99) {
                stateManager.switchTo("none");
                destroy();
            }
        }

    }
    
}
