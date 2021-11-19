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
import scene.Stage;

/**
 * LightningA (magic) class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class LightningC extends Magic {

    private static final int MAX_LIGHTNINGS = 10;
    
    public LightningC(Stage stage, Actor owner) {
        super(stage, "magic_lightning_c", owner);
    }
    
    @Override
    public void create() {
        consumedPotions = 4;
        collider = new Collider(0, 0, 1, 1, 40, 6);
        
        for (int i = 0; i < MAX_LIGHTNINGS; i++) {
            Projectile lightning = new Projectile(
                    stage, "lightning_" + i, "magic_lightning", "ligthning_a");
            
            Animation animation = 
                    lightning.getAnimationPlayer().getAnimation("ligthning_a");

            animation.setParameter("vx", "0.0");
            animation.setParameter("vz", "0.0");
            animation.setParameter("check_height_hit", "false");
            elements.add(lightning);
        }
    }

    @Override
    public void use() {
        int enemyIndex = 0;
        for (Actor actor : stage.getActors()) {
            if (actor.isMagicHittable() 
                    && actor.getControl() == IA && !actor.isMountable()) {
                
                elements.get(enemyIndex++).spawn(owner, RIGHT
                    , actor.getWx(), actor.getWy(), actor.getWz());
            }
        }
        // not used lightinings, choose a random place in the terrain
        int freeLightningCount = 4 - enemyIndex;
        for (int i = 0; i < freeLightningCount; i++) {
            int distance = CANVAS_WIDTH / freeLightningCount;
            Projectile lightning = elements.get(enemyIndex++);
            lightning.spawn(owner, RIGHT
                , owner.getWx(), owner.getWy(), owner.getWz());
            
            lightning.setWx(Camera.getX() + distance * i 
                                + distance / 2 + Util.random(-32, 32));
            
            int ez = Terrain.getMagicPathZ((int) lightning.getWx());
            lightning.setWz(ez + Util.random(-32, 32));            
        }
        destroyed = false;
        stage.addActor(this);
        Audio.playSound("magic_lightning");
        finishedTime = Util.getTime() + 4000;
    }

}
