package infra;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 * BackgroundTransition class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class BackgroundTransition {

    protected boolean enabled = true;
    
    protected final Map<String, Background> backgrounds = new HashMap<>();
    protected Background currentBackground;
    protected Background transitionBackground;

    protected double transitionSpeed;
    protected double transitionProgress = 1.0;
    protected boolean transitionStarted;
    protected long transitionStartTime;

    public BackgroundTransition() {
    }
    
    public void reset() {
        backgrounds.clear();
        currentBackground = null;
        transitionBackground = null;
        transitionSpeed = 0.02;
        transitionProgress = 2.0;
        transitionStarted = false;
        transitionStartTime = 0;
    }
    
    public boolean isFinished() {
        return transitionProgress >= 2.0;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public void addBackground(String transitionId, Background background) {
        backgrounds.put(transitionId, background);
        if (currentBackground == null) {
            currentBackground = background;
        }
    }
    
    public void startTransition(
            String transitionId, double transitionSpeed, long delayTime) {
        
        transitionBackground = backgrounds.get(transitionId);
        transitionProgress = 0.0;
        this.transitionSpeed = transitionSpeed;
        transitionStartTime = Util.getTime() + delayTime;
        transitionStarted = false;
    }

    public void startVelocityTransition(String backgroundId
            , double targetVx, double targetVy, double transitionSpeed) {
        
        backgrounds.get(backgroundId)
                .startVelocityTransition(targetVx, targetVy, transitionSpeed);
    }
    
    public void update() {
        if (!enabled) {
            return;
        }
        backgrounds.values().forEach(background -> background.update());
        if (transitionProgress < 2.0 && !transitionStarted) {
             if (Util.getTime() >= transitionStartTime) {
                 transitionStarted = true;
             }
        }
        else if (transitionProgress < 2.0 && transitionStarted) {
            transitionProgress += transitionSpeed;
            if (transitionProgress >= 2.0) {
                transitionProgress = 2.0;
                currentBackground = transitionBackground;
                transitionBackground = null;
            }
        }
    }
    
    public void draw(Graphics2D g, double cameraX, double cameraY) {
        if (!enabled) {
            return;
        }
        if (transitionProgress < 1.0) {
            if (currentBackground != null) {
                currentBackground.draw(g, cameraX, cameraY);
            }
            if (transitionBackground != null) {
                Composite originalComposite = g.getComposite();
                int transitionIndex = (int) (transitionProgress * 255);
                Composite tc = Transparency.getComposite(transitionIndex);
                g.setComposite(tc);
                transitionBackground.draw(g, cameraX, cameraY);
                g.setComposite(originalComposite);
            }
        }
        else if (transitionProgress < 2.0) {
            if (currentBackground != null) {
                Composite originalComposite = g.getComposite();
                int transitionIndex = (int) ((2.0 - transitionProgress) * 255);
                Composite tc = Transparency.getComposite(transitionIndex);
                g.setComposite(tc);
                currentBackground.draw(g, cameraX, cameraY);
                g.setComposite(originalComposite);
            }
            if (transitionBackground != null) {
                transitionBackground.draw(g, cameraX, cameraY);
            }
        }
        else if (transitionProgress >= 2.0 && currentBackground != null) {
            currentBackground.draw(g, cameraX, cameraY);
        }
    }

}
