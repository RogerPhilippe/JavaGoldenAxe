package infra;

import java.awt.Graphics2D;
import java.util.Map;

/**
 * AnimationPlayer class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class AnimationPlayer {
    
    private final Map<String, String> parameters;
    private final Map<String, Animation> animations;
    private Animation currentAnimation = null;
    
    public AnimationPlayer(Map<String, String> parameters
                                , Map<String, Animation> animations) {
        
        this.parameters = parameters;
        this.animations = animations;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public boolean isParameterAvailable(String name) {
        return parameters.get(name) != null;
    }
    
    public boolean isAnimationAvailable(String animationId) {
        return animations.containsKey(animationId);
    }

    public Animation getCurrentAnimation() {
        return currentAnimation;
    }

    public Animation getAnimation(String animationId) {
        return animations.get(animationId);
    }

    public void setAnimation(String animationId) {
        setAnimation(animationId, true);
    }

    public void setAnimation(String animationId, boolean resetFrame) {
        currentAnimation = animations.get(animationId);
        currentAnimation.clearReactedActors();
        if (resetFrame && currentAnimation != null) {
            currentAnimation.setCurrentFrameIndex(0.0);
        }
    }

    public void update() {
        if (currentAnimation != null) {
            currentAnimation.update();
        }
    }

    public void draw(Graphics2D g, double wx, double wy, double wz
            , int cameraX, int cameraY, Direction direction, boolean blink
                , double scaleX, double scaleY, double angle) {
        
        if (currentAnimation != null) {
            currentAnimation.draw(g, wx, wy, wz, cameraX
                    , cameraY, direction, blink, scaleX, scaleY, angle);
        }
    }

    public void drawShadow(Offscreen offscreen, double wx, double wy, double wz
                , int cameraX, int cameraY, Direction direction
                    , double jumpHeight, double shear) {
        
        if (currentAnimation != null) {
            currentAnimation.drawShadow(offscreen, wx, wy, wz
                            , cameraX, cameraY, direction, jumpHeight, shear);
        }
    }

    public void setCurrentAnimationFrameIndex(double frameIndex) {
        if (currentAnimation != null) {
            currentAnimation.setCurrentFrameIndex(frameIndex);
        }
    }

    public void setCurrentAnimationSpeed(double speed) {
        if (currentAnimation != null) {
            currentAnimation.setSpeed(speed);
        }
    }

    public double getCurrentAnimationFrameIndex() {
        if (currentAnimation == null) {
            return -1;
        }
        return currentAnimation.getCurrentFrameIndex();
    }

    public void setCurrentAnimationLooping(boolean looping) {
        if (currentAnimation != null) {
            currentAnimation.setLooping(looping);
        }
    }
    
}
