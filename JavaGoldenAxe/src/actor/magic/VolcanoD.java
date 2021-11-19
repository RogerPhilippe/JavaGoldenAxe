package actor.magic;

import actor.Projectile;
import actor.Projectile.ProjectileState;
import infra.Actor;
import infra.Animation;
import infra.Audio;
import infra.Camera;
import infra.Collider;
import infra.Magic;
import static infra.Settings.*;
import infra.Terrain;
import infra.Util;
import java.util.ArrayList;
import java.util.List;
import scene.Stage;

/**
 * VolcanoD (magic) class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class VolcanoD extends Magic {

    private static final int MAX_ROCKS_COUNT = 20;
    
    private Projectile explosionBig;
    private int explosionIndex;
    private long explosionNextIndexTime;
    
    private final List<Projectile> rocks = new ArrayList<>();
    private long nextSpawnRockTime;
    
    public VolcanoD(Stage stage, Actor owner) {
        super(stage, "magic_volcano_d", owner);
    }
    
    @Override
    public void create() {
        consumedPotions = 6;
        collider = new Collider(0, 0, 1, 1, 80, 8);
        
        explosionBig = new Projectile(stage
            , "magic_explosion_big", "magic_volcano", "explosion_big_0");
        
        elements.add(explosionBig);
        
        for (int i = 0; i < MAX_ROCKS_COUNT; i++) {
            Projectile rock = new Projectile(stage
                , "rock_" + i, "magic_volcano", "empty");
            
            rock.setDestroyed(true);
            elements.add(rock);
            rocks.add(rock);
        }
    }

    @Override
    public boolean isFinished() {
        return explosionIndex >= 13;
    }
    
    @Override
    public void fixedUpdate() {
        super.fixedUpdate();
        
        if (Util.getTime() >= explosionNextIndexTime) {
            explosionIndex++;
            explosionNextIndexTime = Util.getTime() + 500;
                    
            if (explosionIndex > 5) {
                if (explosionIndex == 10) {
                    explosionBig.getAnimationPlayer()
                            .setAnimation("explosion_big_6");
                }
            }
            else {
                Audio.playSound("magic_volcano_d");
                Audio.playSound("magic_volcano_abc");
                explosionBig.getAnimationPlayer()
                        .setAnimation("explosion_big_" + explosionIndex);
            }
            
        }
        
        // spawn rocks
        if (explosionIndex >= 4 && explosionIndex <= 10) {
            for (Projectile rock : rocks) {
                if (rock.isDestroyed() 
                        && Util.getTime() >= nextSpawnRockTime) {

                    ProjectileState projectileState = 
                        (ProjectileState) rock.getStateManager()
                                                    .getState("animating");                        

                    String rockId = "rock_" + Util.random(2);
                    projectileState.setStateAnimationId(rockId);
                    Animation animation = 
                        rock.getAnimationPlayer().getAnimation(rockId);

                    animation.setParameter("vx", "" + Util.random(-2, 2));
                    animation.setParameter("vy", "" + Util.random(-7, -4));
                    animation.setParameter("vz", "0.0");

                    rock.spawn(owner, direction
                        , owner.getWx(), owner.getWy(), owner.getWz());

                    rock.setWx(explosionBig.getWx() + Util.random(-16, 16));
                    rock.setWz(explosionBig.getWz() + 1.0);
                    rock.setWy(
                        explosionBig.getWy() - 80.0 + Util.random(-16, 16));

                    nextSpawnRockTime = Util.getTime() + 200;
                }
            }
        }
    }
    
    @Override
    public void use() {
        destroyed = false;
        stage.addActor(this);
        explosionBig.spawn(owner, direction
                , owner.getWx(), owner.getWy(), owner.getWz());
        
        explosionBig.setWx(Camera.getX() + CANVAS_WIDTH / 2);
        double ez = Terrain.getMagicPathZ((int) explosionBig.getWx());
        explosionBig.setWz(ez);
        
        double ey = Terrain.getHeight(
                explosionBig.getWx(), explosionBig.getWz());
        
        if (ey < Terrain.getDeathHeight()) {
            explosionBig.setWy(ey);
        }
        
        explosionIndex = 0;
        explosionNextIndexTime = Util.getTime() + 500;
        Audio.playSound("magic_volcano_abc");
        
        nextSpawnRockTime = Util.getTime() + 100;
    }

}
