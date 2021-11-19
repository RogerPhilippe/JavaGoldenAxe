package infra;

import java.awt.Composite;
import java.awt.Graphics2D;

/**
 * MagicBackgroundTransition class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class MagicBackgroundTransition extends BackgroundTransition {
    
    private static final int DARKNESS = 96;
    
    private final Background magicBackground;
    
    public MagicBackgroundTransition() {
        super();
        magicBackground = new Background(
                "magic_layer", 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, true);
    }

    @Override
    public void reset() {
        super.reset(); 
        backgrounds.put("magic", magicBackground);
    }
    
    @Override
    public void draw(Graphics2D g, double cameraX, double cameraY) {
        if (!enabled) {
            return;
        }
        if (transitionProgress < 1.0) {
            Composite originalComposite = g.getComposite();
            if (currentBackground != null) {
                Composite tc = Transparency.getComposite(255 - DARKNESS);
                g.setComposite(tc);
                currentBackground.draw(g, cameraX, cameraY);
            }
            if (transitionBackground != null) {
                int transitionIndex = 
                        (int) (transitionProgress * (255 - DARKNESS));
                
                Composite tc = Transparency.getComposite(transitionIndex);
                g.setComposite(tc);
                transitionBackground.draw(g, cameraX, cameraY);
            }
            g.setComposite(originalComposite);
        }
        else if (transitionProgress < 2.0) {
            Composite originalComposite = g.getComposite();
            if (currentBackground != null) {
                int transitionIndex = 
                        (int) ((2.0 - transitionProgress) * (255 - DARKNESS));
                
                Composite tc = Transparency.getComposite(transitionIndex);
                g.setComposite(tc);
                currentBackground.draw(g, cameraX, cameraY);
            }
            if (transitionBackground != null) {
                Composite tc = Transparency.getComposite(255 - DARKNESS);
                g.setComposite(tc);
                transitionBackground.draw(g, cameraX, cameraY);
            }
            g.setComposite(originalComposite);
        }
        else if (transitionProgress >= 2.0 && currentBackground != null) {
            Composite originalComposite = g.getComposite();
            Composite tc = Transparency.getComposite(255 - DARKNESS);
            g.setComposite(tc);
            currentBackground.draw(g, cameraX, cameraY);
            g.setComposite(originalComposite);
        }
    }

}
