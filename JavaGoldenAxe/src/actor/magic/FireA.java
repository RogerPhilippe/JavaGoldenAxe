package actor.magic;

import actor.Projectile;
import infra.Actor;
import static infra.Actor.Control.IA;
import infra.Animation;
import infra.Audio;
import infra.Camera;
import infra.Collider;
import static infra.Direction.*;
import infra.Magic;
import static infra.Settings.*;
import infra.Terrain;
import infra.Util;
import java.util.ArrayList;
import java.util.List;
import scene.Stage;

/**
 * FireA (magic) class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class FireA extends Magic {

    private class Fire {
        
        int index;
        Projectile fire;

        long startTime;
        double lerp = 0.0;

        double ox;
        double oy;
        
        // part 1 - rotation
        double p1x;
        double p1OffsetX;
        double p1y;
        double p1vx;
        double p1vy;
        double p1Radius;
        
        // part 2 - rotation
        double p2x;
        double p2y;
        double p2Radius;
        double p2Angle;
        double p2AngularVelocity;
        
        Fire(int index, Projectile fire) {
            this.index = index;
            this.fire = fire;
        }
        
        double getX() {
            return getP1X() + lerp * (getP2X() - getP1X());
        }

        double getP1X() {
            return p1x + p1OffsetX;
        }

        double getP2X() {
            return ox + p2Radius * Math.cos(p2Angle);
        }
        
        double getY() {
            return getP1Y() + lerp * (getP2Y() - getP1Y());
        }

        double getP1Y() {
            return p1y;
        }

        double getP2Y() {
            return oy + p2Radius * Math.sin(p2Angle);
        }
        
        void start() {
            p1x = owner.getWx(); 
            p1y = owner.getWy() + owner.getWz() - 1000.0 - 64.0;
            p1vx = 0.0;
            p1vy = -2.5;
            p1OffsetX = 0.0;
            p1Radius = 0.0;
            
            p2x = 0.0;
            p2y = 0.0;
            p2Radius = Util.random(16, 48);
            p2Angle = 2 * Math.PI * Math.random();
            p2AngularVelocity = 0.2 * Math.random() - 0.1; 
            
            lerp = 0.0;
            startTime = Util.getTime() + 100 * index;
        }

        void update() {
            if (Util.getTime() < startTime) {
                return;
            }
            
            double difX = (Camera.getX() + CANVAS_WIDTH / 2) - p1x;
            if (Math.abs(difX) > 2.0) {
                p1x += Math.signum(difX);
            }
            p1vy += 0.05;
            p1y += p1vy;
            if (p1vy > 0.0) {
                p1OffsetX = p1Radius * Math.sin(6.0 * p1vy);
                p1Radius += 1.0;
            }
            if (p1vy > 2.0) {
                lerp += 0.01;
                if (lerp > 1.0) {
                    lerp = 1.0;
                }
            }
            
            p2Angle += p2AngularVelocity;
            fire.setWx(getX());
            fire.setWy(getY());
            fire.setWz(1000);
        }
        
    }
    
    private static final int MAX_FIRES = 12;
    private static final int MAX_FLAMES = 10;
    
    private final List<Fire> fires = new ArrayList<>();
    private final List<Projectile> flames = new ArrayList<>();
    
    public FireA(Stage stage, Actor owner) {
        super(stage, "magic_fire_a", owner);
    }
    
    @Override
    public void create() {
        consumedPotions = 6;
        collider = new Collider(0, 0, 1, 1, 10, 2);
        
        for (int i = 0; i < MAX_FIRES; i++) {
            Projectile fire = new Projectile(
                        stage, "fire_" + i, "magic_fire", "fire_0");
            
            Animation animation = 
                        fire.getAnimationPlayer().getAnimation("fire_0");
            
            animation.setCurrentFrameIndex(11.0 * Math.random());
            elements.add(fire);
            fires.add(new Fire(i, fire));
        }

        for (int i = 0; i < MAX_FLAMES; i++) {
            Projectile flame = new Projectile(
                        stage, "flame_" + i, "magic_fire", "fire_1");
            
            elements.add(flame);
            flames.add(flame);
        }
    }

    @Override
    public void fixedUpdate() {
        super.fixedUpdate(); 
        fires.forEach(fire -> fire.update());
    }
    
    @Override
    public void use() {
        int enemyIndex = 0;
        for (Actor actor : stage.getActors()) {
            if (actor.isMagicHittable() 
                    && actor.getControl() == IA && !actor.isMountable()) {
                
                flames.get(enemyIndex++).spawn(owner, RIGHT
                    , actor.getWx(), actor.getWy(), actor.getWz() + 1.0);
            }
        }
        // not used flames, choose a random place in the terrain
        int freeFireCount = 4 - enemyIndex;
        for (int i = 0; i < freeFireCount; i++) {
            int distance = CANVAS_WIDTH / freeFireCount;
            Projectile flame = flames.get(enemyIndex++);
            flame.spawn(owner, RIGHT
                , owner.getWx(), owner.getWy(), owner.getWz());
            
            flame.setWx(Camera.getX() + distance * i 
                                + distance / 2 + Util.random(-32, 32));
            
            int ez = Terrain.getMagicPathZ((int) flame.getWx());
            flame.setWz(ez + Util.random(-32, 32));            
        }
        // fires
        int cellWidth = CANVAS_WIDTH / 4;
        int cellHeight = CANVAS_HEIGHT / 4;
        for (int i = 0; i < 12; i++) {
            Fire fire = fires.get(i);
            int row = i / 4;
            int col = i % 4;
            fire.fire.spawn(owner, RIGHT
                    , owner.getWx(), owner.getWy(), owner.getWz());
            
            fire.ox = Camera.getX() + cellWidth * col 
                +  Util.random(-cellWidth / 4, cellWidth / 4) + cellWidth / 2;
            
            fire.oy = Camera.getY() + cellHeight * row 
                    + Util.random(-cellHeight / 4, cellHeight / 4) - 1000;
            
            fire.fire.setWz(1000);
            fire.start();
        }
        
        destroyed = false;
        stage.addActor(this);
        Audio.playSound("magic_fire_a");
        finishedTime = Util.getTime() + 7000;
    }

}
