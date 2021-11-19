package actor.magic;

import actor.Projectile;
import infra.Actor;
import infra.Animation;
import infra.Audio;
import infra.Camera;
import infra.Collider;
import static infra.Direction.*;
import infra.Magic;
import infra.Terrain;
import scene.Stage;

/**
 * LightningB (magic) class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class LightningB extends Magic {
    
    private final Projectile[] lightnings = new Projectile[2];
    private double progress;
    private boolean sound1Played;
    private boolean sound2Played;
    
    public LightningB(Stage stage, Actor owner) {
        super(stage, "magic_lightning_b", owner);
    }
    
    @Override
    public void create() {
        consumedPotions = 3;
        collider = new Collider(0, 0, 1, 1, 30, 4);

        for (int i = 0; i < 2; i++) {
            lightnings[i] = new Projectile(stage
                    , "lightning_" + i, "magic_lightning", "ligthning_b");

            Animation animation = lightnings[i]
                    .getAnimationPlayer().getAnimation("ligthning_b");

            animation.setParameter("vx", "0.0");
            animation.setParameter("vz", "0.0");
            animation.setParameter("check_height_hit", "false");
            elements.add(lightnings[i]);
        }
    }

    @Override
    public boolean isFinished() {
        return progress > 6.01;
    }
    
    @Override
    public void fixedUpdate() {
        super.fixedUpdate();
        
        for (int i = 0; i < 2; i++) {
            Projectile lightning = lightnings[i];
            double lx = Camera.getX() + 136 
                    + 170 * Math.cos(progress + i * 0.25);
            
            double mainPathZ = Terrain.getMagicPathZ((int) lx);
            double lz = mainPathZ + 16 * Math.sin(lx * 0.035);
            
            lightning.setWx(lx);
            lightning.setWz(lz);
        }
        progress += 0.015;
        if (progress > 0.10 && !sound1Played) {
            sound1Played = true;
            Audio.playSound("magic_lightning");    
        }
        if (progress > 3.25 && !sound2Played) {
            sound2Played = true;
            Audio.playSound("magic_lightning");    
        }
    }

    @Override
    public void use() {
        elements.forEach(part -> {
            part.spawn(
                owner, RIGHT, owner.getWx(), owner.getWy(), owner.getWz());
        });
        progress = 0;
        sound1Played = false;
        sound2Played = false;
        destroyed = false;
        stage.addActor(this);
    }

}
