package actor.magic;

import actor.Projectile;
import infra.Actor;
import infra.Audio;
import infra.Camera;
import infra.Collider;
import static infra.Direction.*;
import infra.Magic;
import static infra.Settings.*;
import infra.Terrain;
import infra.Util;
import java.util.Collections;
import scene.Stage;

/**
 * FireB (magic) class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class FireB extends Magic {
    
    private int explosionCount;
    private long nextExplosionTime;
    
    public FireB(Stage stage, Actor owner) {
        super(stage, "magic_fire_b", owner);
    }
    
    @Override
    public void create() {
        consumedPotions = 6;
        collider = new Collider(0, 0, 1, 1, 20, 2);
        
        for (int i = 0; i < 4; i++) {
            Projectile fire = new Projectile(
                    stage, "fire_" + i, "magic_fire", "fire_1");
            
            elements.add(fire);
        }
    }

    @Override
    public void fixedUpdate() {
        super.fixedUpdate(); 

        if (explosionCount < 40 && Util.getTime() >= nextExplosionTime) {
            Collections.shuffle(elements);
            for (Projectile fireCandidate : elements) {
                if (fireCandidate.getAnimationPlayer()
                        .getCurrentAnimation().getName().equals("fire_1")) {
                    
                    fireCandidate.getAnimationPlayer().setAnimation("fire_2");
                    Audio.playSound("explosion");
                    break;
                }
            }
            explosionCount++;
            nextExplosionTime = Util.getTime() + 500;
        }
    }

    @Override
    public void use() {
        int distance = CANVAS_WIDTH / 4;
        for (int i = 0; i < elements.size(); i++) {
            Projectile fire = elements.get(i);
            fire.spawn(owner, RIGHT
                    , owner.getWx(), owner.getWy(), owner.getWz());
            
            fire.setWx(Camera.getX() + distance * i 
                                + distance / 2 + Util.random(-4, 4));
            
            int ez = Terrain.getMagicPathZ((int) fire.getWx());
            fire.setWz(ez);
        }
        destroyed = false;
        stage.addActor(this);
        Audio.playSound("magic_fire_b");
        finishedTime = Util.getTime() + 3500;
        explosionCount = 0;
        nextExplosionTime = Util.getTime() + 1500;
    }

}
