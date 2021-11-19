package actor.magic;

import actor.Projectile;
import infra.Actor;
import infra.Animation;
import infra.Audio;
import infra.Collider;
import static infra.Direction.*;
import infra.Magic;
import infra.Util;
import scene.Stage;

/**
 * LightningA (magic) class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class LightningA extends Magic {

    public LightningA(Stage stage, Actor owner) {
        super(stage, "magic_lightning_a", owner);
    }
    
    @Override
    public void create() {
        consumedPotions = 1;
        collider = new Collider(0, 0, 1, 1, 20, 2);
        
        Projectile lightning = new Projectile(
                        stage, "lightning", "magic_lightning", "ligthning_a");
        {
            Animation animation = 
                    lightning.getAnimationPlayer().getAnimation("ligthning_a");
            
            animation.setParameter("vx", "0.0");
            animation.setParameter("vz", "0.0");
            animation.setParameter("check_height_hit", "false");
        }
        elements.add(lightning);
        
        double[] vels = { 1.5, -0.1, 1.6, -0.20,
                          1.5, 0.1, 1.6, 0.20,
                          -1.5, -0.1, -1.6, -0.20,
                          -1.5, 0.1, -1.6, 0.20 };
        
        for (int i = 0; i < 8; i++) {
            Projectile spark = new Projectile(
                    stage, "spark_" + i, "magic_lightning", "spark");
            
            Animation animation = 
                    spark.getAnimationPlayer().getAnimation("spark");
            
            animation.setParameter("vx", "" + vels[(i * 2)]);
            animation.setParameter("vz", "" + vels[(i * 2) + 1]);
            animation.setParameter("check_height_hit", "true");
            
            elements.add(spark);
        }
    }


    @Override
    public void use() {
        elements.forEach(part -> {
            if (part.getId().equals("lightning")) {
                part.spawn(owner, RIGHT
                        , owner.getWx(), owner.getWy(), owner.getWz());
            }
            else {
                part.spawn(owner, RIGHT
                        , owner.getWx(), owner.getWy() - 1.0, owner.getWz());
            }
        });
        destroyed = false;
        stage.addActor(this);
        Audio.playSound("magic_lightning");
        finishedTime = Util.getTime() + 3000;
    }

}
