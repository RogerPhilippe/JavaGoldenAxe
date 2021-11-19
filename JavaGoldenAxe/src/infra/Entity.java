package infra;

import java.awt.Graphics2D;
import scene.Stage;

/**
 * Entity class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Entity {
    
    protected final Stage stage;
    protected String id;
    protected boolean started;
    protected boolean destroyed;
    protected boolean visible = true;
    
    public Entity(Stage stage, String id) {
        this.stage = stage;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public void destroy() {
        destroyed = true;
        onDestroy();
    }

    public void setDestroyed(boolean destroyed) {
        this.destroyed = destroyed;
    }
    
    public void onDestroy() {
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    // invoked only once, even if the same entity is added 
    // and removed multiple times in the stage scene
    public void start() {
    }
    
    public void fixedUpdate() {
    }
    
    public void drawShadow(Offscreen offscreen
            , int cameraX, int cameraY, double shear) {
    }
    
    public void draw(Graphics2D g, int cameraX, int cameraY) {
    }
    
}
