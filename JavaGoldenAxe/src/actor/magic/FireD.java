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
import infra.Util;
import scene.Stage;

/**
 * FireD (magic) class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class FireD extends Magic {

    public FireD(Stage stage, Actor owner) {
        super(stage, "magic_fire_d", owner);
    }
    
    @Override
    public void create() {
        consumedPotions = 6;
        collider = new Collider(0, 0, 1, 1, 40, 4);
        
        double[] vels = { 1.5, -0.01, 1.5, -0.01,
                          1.51, 0.0, 1.52, 0.0,
                          1.51, 0.0, 1.52, 0.0,
                          1.5, 0.01, 1.5, 0.01 };
        
        for (int i = 0; i < 8; i++) {
            Projectile fire = new Projectile(
                    stage, "fire_" + i, "magic_fire", "fire_4");
            
            Animation animation = 
                    fire.getAnimationPlayer().getAnimation("fire_4");
            
            animation.setCurrentFrameIndex(Util.random(2));
            animation.setParameter("vx", "" + vels[(i * 2)]);
            animation.setParameter("vz", "" + vels[(i * 2) + 1]);
            animation.setParameter("", "true");
            
            elements.add(fire);
        }
    }

    private final int[] fireOffsetsZ = { 8, -8, 16, -16, 24, -24, 32, -32 };
    
    @Override
    public void use() {
        for (int i = 0; i < elements.size(); i++) {
            Projectile element = elements.get(i);
            element.spawn(owner, RIGHT
                    , owner.getWx(), owner.getWy() - 1.0, owner.getWz());
            
            element.setWx(Camera.getX() - 32 + Util.random(-32, 32));
            int ez = Terrain.getMagicPathZ((int) element.getWx());
            element.setWz(ez + fireOffsetsZ[i] + Util.random(-32, 32));
        }
        destroyed = false;
        stage.addActor(this);
        Audio.playSound("magic_fire_d");
        finishedTime = Util.getTime() + 4500;
    }

}
