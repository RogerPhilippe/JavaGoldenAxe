package infra;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Background class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Background {
    
    protected final BufferedImage backgroundImage;
    
    protected double scrollX;
    protected double scrollY;
    protected final double cameraOffsetX;
    protected final double cameraOffsetY;
    protected final double cameraScaleX;
    protected final double cameraScaleY;
    protected final boolean tiled;

    protected double scrollVx;
    protected double scrollVy;
    protected double scrollTargetVx;
    protected double scrollTargetVy;
    protected double scrollVelocityTransitionSpeedX;
    protected double scrollVelocityTransitionSpeedY;

    public Background(String imageRes, double scrollVx, double scrollVy
            , double cameraOffsetX, double cameraOffsetY
                , double cameraScaleX, double cameraScaleY, boolean tiled) {

        this.backgroundImage = Resource.getImage(imageRes);
        this.scrollX = 0.0;
        this.scrollY = 0.0;
        this.cameraOffsetX = cameraOffsetX;
        this.cameraOffsetY = cameraOffsetY;
        this.cameraScaleX = cameraScaleX;
        this.cameraScaleY = cameraScaleY;
        this.tiled = tiled;

        this.scrollVx = scrollVx;
        this.scrollVy = scrollVy;
        this.scrollTargetVx = scrollVx;
        this.scrollTargetVy = scrollVy;
        this.scrollVelocityTransitionSpeedX = 0.0;
        this.scrollVelocityTransitionSpeedY = 0.0;
    }
    
    public void startVelocityTransition(
            double targetVx, double targetVy, double transitionSpeed) {
        
        scrollTargetVx = targetVx;
        scrollTargetVy = targetVy;
        double difVx = Math.abs(scrollTargetVx - scrollVx);
        double difVy = Math.abs(scrollTargetVy - scrollVy);
        double length = Math.sqrt(difVx * difVx + difVy * difVy);
        scrollVelocityTransitionSpeedX = 0.0;
        scrollVelocityTransitionSpeedY = 0.0;
        if (length != 0) {
            scrollVelocityTransitionSpeedX = transitionSpeed * (difVx / length);
            scrollVelocityTransitionSpeedY = transitionSpeed * (difVy / length);
        }
    }
    
    public void update() {
        double difVx = scrollTargetVx - scrollVx;
        if (difVx != 0.0 && Math.abs(difVx) >= scrollVelocityTransitionSpeedX) {
            scrollVx += Math.signum(difVx) * scrollVelocityTransitionSpeedX;
        } 
        else {
            scrollVx = scrollTargetVx;
        }
        double difVy = scrollTargetVy - scrollVy;
        if (difVy != 0.0 && Math.abs(difVy) >= scrollVelocityTransitionSpeedY) {
            scrollVy += Math.signum(difVy) * scrollVelocityTransitionSpeedY;
        } 
        else {
            scrollVy = scrollTargetVy;
        }
        scrollX += scrollVx;
        scrollY += scrollVy;
    }
    
    public void draw(Graphics2D g, double cameraX, double cameraY) {
        if (backgroundImage == null) {
            return;
        }
        int xi = (int) (scrollX - cameraOffsetX + cameraScaleX * (int) cameraX);
        int yi = (int) (scrollY - cameraOffsetY + cameraScaleY * (int) cameraY);
        int siw = backgroundImage.getWidth();
        int sih = backgroundImage.getHeight();
        if (tiled) {
            xi = xi % siw;
            yi = yi % sih;
            if (xi < 0) xi += siw;
            if (yi < 0) yi += sih;
            g.drawImage(backgroundImage
                    , siw - xi, sih - yi, siw, sih, 0, 0, xi, yi, null);

            g.drawImage(backgroundImage
                    , siw - xi, 0, siw, sih - yi, 0, yi, xi, sih, null);
            
            g.drawImage(backgroundImage
                    , 0, sih - yi, siw - xi, sih, xi, 0, siw, yi, null);
        }
        g.drawImage(backgroundImage
                , 0, 0, siw - xi, sih - yi, xi, yi, siw, sih, null);
    }

}
