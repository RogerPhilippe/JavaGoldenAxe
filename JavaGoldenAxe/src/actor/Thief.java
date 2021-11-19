package actor;

import static actor.Item.Type.*;
import infra.Actor;
import static infra.Actor.Control.*;
import infra.Audio;
import infra.Camera;
import infra.Collider;
import infra.Collider.Reaction;
import static infra.Direction.*;
import infra.Settings;
import infra.Terrain;
import java.util.ArrayList;
import java.util.List;
import scene.Stage;

/**
 * Thief class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Thief extends Actor {
    
    private static final double MAX_JUMP_HEIGHT = 48;
    
    public static enum Type { POTION, FOOD }
    
    private Type type;
    private int itemCount;
    private long lifeTime = -1;
    private boolean lifeTimeEnabled = false;
    private final List<Item> items = new ArrayList<>();
    
    public Thief(Stage stage, String thiefId, String[] data) {
        super(stage, thiefId, 0, 0
                , "thief_" + data[7].toLowerCase(), null, null);
        
        spawnInternal(data);
        lifeTimeEnabled = true;
        control = IA;
    }
    
    @Override
    public void spawn(String[] data) {
        spawnInternal(data);
    }
    
    private void spawnInternal(String[] data) {
        // locking_camera_id wx wz POTION|FOOD item_count
        if (data != null) {
            lockingCameraId = data[4];
            wx = Integer.parseInt(data[5]);
            wz = Integer.parseInt(data[6]);
            type = Type.valueOf(data[7]);
            itemCount = Integer.parseInt(data[8]);
            for (int i = 0; i < itemCount; i++) {
                switch (type) {
                    case FOOD -> items.add(new Item(stage, LIFE_FOOD));
                    case POTION -> items.add(new Item(stage, MAGIC_POTION));
                }
            }
        }

        destroyIfNotAlive = true;
        control = Control.NONE;
        stateManager.addState(new Idle());    
        stateManager.addState(new Walking());    
        stateManager.addState(new Hit());    
        stateManager.addState(new RunAway());    
        stateManager.switchTo("idle");
    }

    @Override
    public void start() {
        if (lifeTimeEnabled) {
            lifeTime = getTime() + 20000;
        }
    }

    @Override
    public void react(
            Reaction reaction, Collider sourceCollider, Actor sourceActor) {
        
        switch (reaction) {
            case STUNNED_1, STUNNED_2, KNOCK_DOWN -> {
                if (itemCount > 0) {
                    itemCount--;
                    Audio.playSound("hit_knock_down");
                    Audio.playSound("spawn_item");
                    vx = sourceActor.getDirection().getDx() * 2.0;
                    stateManager.switchTo("hit");
                    sourceActor.setVx(0.25);
                }
            }
            default -> {
            }
        }
    }
   
    @Override
    public void fixedUpdate() {
        super.fixedUpdate(); 
        String currentStateName = stateManager.getCurrentState().getName();
        if (lifeTime > 0 && getTime() >= lifeTime 
            && !currentStateName.equals("hit")
                && !currentStateName.equals("run_away")) {
            
            stateManager.switchTo("run_away");
        }
    }

    @Override
    public boolean isMagicHittable() {
        return true;
    }
    
    // --- states ---

    private class Idle extends ActorState {
        
        long moveTime;
        
        public Idle() {
            super("idle");
        }

        @Override
        public void onEnter() {
            moveTime = getTime() + 1000 + (long) (1000 * Math.random());
            String bagState = itemCount > 0 ? "bag_full" : "bag_empty";
            animationPlayer.setAnimation("idle_" + bagState);
            animationPlayer.setCurrentAnimationSpeed(0.2);
            animationPlayer.setCurrentAnimationLooping(true);
        }

        @Override
        public void fixedUpdate() {
            vx = 0.0;
            vz = 0.0;
            updateMovement();
            animationPlayer.update();
            if (getTime() >= moveTime) {
                stateManager.switchTo("walking");
            }
        }
        
    }
    
    private class Walking extends ActorState {
        
        double targetX;
        double targetZ;
        
        public Walking() {
            super("walking");
        }

        @Override
        public void onEnter() {
            // TODO improve this part later
            do {
                double rz = 32 * Math.random() - 16;
                double rx = 16 + (Settings.CANVAS_WIDTH - 32) * Math.random();
                targetZ = wz + Math.signum(rz) * 16 + rz;
                targetX = Camera.getX() + rx;
            } 
            while (!Terrain.isWalkable(targetX, targetZ)
                || Terrain.getHeight(targetX, targetZ) >= Terrain.MAX_HEIGHT);
            
            String bagState = itemCount > 0 ? "bag_full" : "bag_empty";
            animationPlayer.setAnimation("walking_" + bagState);
            animationPlayer.setCurrentAnimationSpeed(0.4);
            animationPlayer.setCurrentAnimationLooping(true);
        }
        
        @Override
        public void fixedUpdate() {
            double difX = targetX - wx;
            double difZ = targetZ - wz;
            double difLength = Math.sqrt(difX * difX + difZ * difZ);
            vx = 0.0;
            vz = 0.0;
            if (difLength >= 4.0) {
                vz = onFloor ? 4.0 * (difZ / difLength) : 0.0;
                vx = 4.0 * (difX / difLength);
                if (!Terrain.isWalkable(wx + vx, wz + vz)
                    || Terrain.getHeight(wx + vx, wz + vz) 
                        >= Terrain.MAX_HEIGHT) {
                    
                    vx = 0.0;
                    vz = 0.0;
                }
                direction = vx < 0.0 ? LEFT : vx > 0.0 ? RIGHT : direction;
            }
            double previousWx = wx;
            double previousWz = wz;
            avoidGetStuckOnTerrain();
            updateMovement();
            if (onFloor && ((vx == 0.0 && vz == 0.0) 
                    || previousWx == wx || previousWz == wz)) {
                
                stateManager.switchTo("idle");
            }
            animationPlayer.update();
        }

        private void avoidGetStuckOnTerrain() {
            double tmpX = wx + Math.signum(vx) 
                    * collisionMargin + Math.signum(vx);
            
            double tmpJumpHeight = Terrain.getHeight(tmpX, wz) - wy;
            
            if (onFloor && tmpJumpHeight < 0 
                    && Math.abs(tmpJumpHeight) <= MAX_JUMP_HEIGHT) {
                
                vy = -5.0;
            }
        }
    }

    private class Hit extends ActorState {
        
        public Hit() {
            super("hit");
        }

        @Override
        public void onEnter() {
            String bagState = itemCount > 0 ? "bag_full" : "bag_empty";
            animationPlayer.setAnimation("hit_" + bagState);
            vy = -4.0;
            vz = 0.0;
            // note: vx is set in the react() method
            direction = vx < 0.0 ? LEFT : RIGHT;
            Item item = items.remove(0);
            item.spawn(direction.getOpposite(), wx, wy - 4.0, wz);
            stage.addActor(item);
        }
        
        @Override
        public void fixedUpdate() {
            updateMovement();
            if (vy == 0.0) {
                if (itemCount > 0) {
                    stateManager.switchTo("walking");
                }
                else {
                    stateManager.switchTo("run_away");
                }
            }
        }
        
    }
    
    private class RunAway extends ActorState {
        
        double targetX;
        double targetZ;
        
        public RunAway() {
            super("run_away");
        }

        @Override
        public void onEnter() {
            double rz = 32 * Math.random() - 16;
            targetZ = wz + Math.signum(rz) * 16 + rz;
            targetX = Camera.getX() + 2 * Settings.CANVAS_WIDTH;
            if (Math.random() > 0.5) {
                targetX = Camera.getX() - Settings.CANVAS_WIDTH;
            }
            String bagState = itemCount > 0 ? "bag_full" : "bag_empty";
            animationPlayer.setAnimation("walking_" + bagState);
            animationPlayer.setCurrentAnimationSpeed(0.4);
            animationPlayer.setCurrentAnimationLooping(true);
        }
        
        @Override
        public void fixedUpdate() {
            double difX = targetX - wx;
            double difZ = targetZ - wz;
            vx = 0.0;
            vz = 0.0;
            if (Math.abs(difX) >= 4.0) {
                vx = 5.0 * Math.signum(difX);
                direction = vx < 0.0 ? LEFT : RIGHT;
            }
            if (onFloor && Math.abs(difZ) >= 1.0) {
                vz = 1.0 * Math.signum(difZ);
            }
            avoidGetStuckOnTerrain();
            updateMovement();
            destroyIfOutOfViewLeft();
            destroyIfOutOfViewRight();
            animationPlayer.update();
        }

        private void avoidGetStuckOnTerrain() {
            double tmpX = wx + Math.signum(vx) 
                    * collisionMargin + Math.signum(vx);
            
            double tmpJumpHeight = Terrain.getHeight(tmpX, wz) - wy;
            
            if (!Terrain.isWalkable(tmpX, wz)
                    || (tmpJumpHeight < 0 
                        && Math.abs(tmpJumpHeight) > MAX_JUMP_HEIGHT)) {
                
                targetX = Camera.getX() + 2 * Settings.CANVAS_WIDTH;
                if (vx > 0.0) {
                    targetX = Camera.getX() - Settings.CANVAS_WIDTH;
                }
                targetZ = wz + 128.0;
            }
            else if (onFloor && tmpJumpHeight < 0 
                    && Math.abs(tmpJumpHeight) <= MAX_JUMP_HEIGHT) {
                
                vy = -5.0;
            }
        }
        
    }

    @Override
    public void onDestroy() {
        Camera.removeLockingActor(this);
    }
    
}
