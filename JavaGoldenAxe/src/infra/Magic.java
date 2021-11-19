package infra;

import actor.Projectile;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import scene.Stage;

/**
 * Magic class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Magic extends Actor {

    protected final Actor owner;
    protected final List<Projectile> elements = new ArrayList<>();
    protected long finishedTime;
    protected int damage = 20;
    protected Collider collider;
    protected int consumedPotions;
    
    public Magic(Stage stage, String magicId, Actor owner) {
        super(stage, magicId, 0, 0, null, null, null);
        this.owner = owner;
        collisionCheckEnabled = false;
    }
    
    public void create() {
    }

    public Collider getCollider() {
        return collider;
    }

    public int getConsumedPotions() {
        return consumedPotions;
    }

    public void use() {
    }

    public int getDamage() {
        return damage;
    }
    
    public boolean isFinished() {
        return Util.getTime() >= finishedTime;
    }
    
    @Override
    public void fixedUpdate() {
        if (isFinished()) {
            elements.forEach(part -> part.destroy());
            destroy();
        }
    }
    
    @Override
    public void draw(Graphics2D g, int cameraX, int cameraY) {
        // do nothing here
    }

}
