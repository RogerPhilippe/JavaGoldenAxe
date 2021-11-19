package actor;

import static actor.Item.Type.*;
import infra.Actor;
import infra.Audio;
import infra.Direction;
import java.util.UUID;
import scene.Stage;

/**
 * Item class.
 * 
 * Used to represent magic potion (blue thief) or meat (green thief).
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Item extends Actor {
    
    public static enum Type { MAGIC_POTION, LIFE_FOOD }
    
    private final Type type;
    private int index;
    private int lastIndex;
    
    public Item(Stage stage, Type type) {
        super(stage, "item_" + UUID.randomUUID(), 0, 0, "thief_" 
                + (type == MAGIC_POTION ? "potion" : "food"), null, null);
        
        this.type = type;
        stateManager.addState(new Collectable());
        if (type == MAGIC_POTION) {
            stateManager.addState(new Disintegrating());
        }
        stateManager.switchTo("none");
    }

    @Override
    protected boolean isHorizontalMovementByCameraLimited() {
        return true;
    }
    
    public Type getType() {
        return type;
    }

    @Override
    public void start() {
    }
    
    public void spawn(Direction direction, double wx, double wy, double wz) {
        this.direction = direction;
        this.wx = wx;
        this.wy = wy;
        this.wz = wz;
        stateManager.switchTo("collectable");
    }
    
    public void disintegrate(
            int index, int lastIndex, double wx, double wy, double wz) {
        
        this.index = index;
        this.lastIndex = lastIndex;
        this.wx = wx;
        this.wy = wy;
        this.wz = wz;
        stateManager.switchTo("disintegrating");
        destroyed = false;
        stage.addActor(this);
    }
    
    @Override
    public void pickUp(Actor actor) {
        if (actor instanceof Player player) {
            switch (type) {
                case MAGIC_POTION -> player.addPotion(this);
                case LIFE_FOOD -> player.recoverEnergy();
            }
            Audio.playSound("get_item");
            stateManager.switchTo("none");
            destroy();
        }
    }
    
    
    // --- states ---

    private class Collectable extends ActorState {

        public Collectable() {
            super("collectable");
        }

        @Override
        public void onEnter() {
            animationPlayer.setAnimation(
                    "item_" + type.toString().toLowerCase());
            
            animationPlayer.getCurrentAnimation()
                                    .setCollisionCheckEnabled(false);
            
            vy = -6.0;
            vx = direction.getDx() * 1.0;
        }
        
        @Override
        public void fixedUpdate() {
            double vyCopy = vy;
            updateMovement();
            if (vy == 0.0 && vyCopy != 0.0) {
                vy = -0.5 * vyCopy;
                vx *= 0.5;
                if (Math.abs(vy) < 0.1) {
                    vx = 0.0;
                    vy = 0.0;
                }
            }
            else if (vy > 0.0) {
                animationPlayer.getCurrentAnimation()
                                        .setCollisionCheckEnabled(true);
            }
            destroyIfOutOfViewLeft();
        }
    }
    
    private class Disintegrating extends ActorState {

        public Disintegrating() {
            super("disintegrating");
        }

        @Override
        public void onEnter() {
            animationPlayer.setAnimation(
                "item_" + type.toString().toLowerCase() + "_disintegrating");
            
            vy = -7.0;
            vx = (index - lastIndex / 2.0) * 0.75;
        }
        
        @Override
        public void fixedUpdate() {
            updateMovement();
            if (animationPlayer.getCurrentAnimation().isFinished()) {
                destroy();
            }
            animationPlayer.update();
        }
    }
    
}
