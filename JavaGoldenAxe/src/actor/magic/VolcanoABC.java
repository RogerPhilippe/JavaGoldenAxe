package actor.magic;

import actor.Projectile;
import actor.Projectile.ProjectileState;
import static actor.magic.VolcanoABC.Type.VOLCANO_A;
import infra.Actor;
import infra.Audio;
import infra.Camera;
import infra.Collider;
import infra.Magic;
import static infra.Settings.*;
import infra.Terrain;
import infra.Util;
import scene.Stage;

/**
 * VolcanoABC (magic) class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class VolcanoABC extends Magic {

    private static final int MAX_EXPLOSIONS_X = 5;
    private static final int MAX_EXPLOSIONS_Y = 4;
    private static final int CELL_WIDTH = CANVAS_WIDTH / MAX_EXPLOSIONS_X;
    private static final int CELL_HEIGHT = CANVAS_HEIGHT / MAX_EXPLOSIONS_Y;
    
    public static enum Type { VOLCANO_A("a", 20, 2, 2)
                            , VOLCANO_B("b", 40, 4, 4)
                            , VOLCANO_C("c", 60, 6, 6);

        String letter;
        int damage;
        int score;
        int consumedPotions;
        
        private Type(String letter, int damage, int score, int consumedPotions) {
            this.letter = letter;
            this.damage = damage;
            this.score = score;
            this.consumedPotions = consumedPotions;
        }

    }
    
    private final Type type;
    
    private final Projectile[][] explosions 
            = new Projectile[MAX_EXPLOSIONS_Y][MAX_EXPLOSIONS_X];

    private final long[][] explosionsStartTime 
            = new long[MAX_EXPLOSIONS_Y][MAX_EXPLOSIONS_X];
    
    public VolcanoABC(Stage stage, Actor owner, Type type) {
        super(stage, "magic_volcano_" + VOLCANO_A.letter, owner);
        this.type = type;
    }
    
    @Override
    public void create() {
        consumedPotions = type.consumedPotions;
        collider = new Collider(0, 0, 1, 1
                , 20 * (type.ordinal() + 1), 2 * (type.ordinal() + 1));
        
        for (int row = 0; row < MAX_EXPLOSIONS_Y; row++) {
            for (int col = 0; col < MAX_EXPLOSIONS_X; col++) {
                explosions[row][col] = new Projectile(stage
                        , "magic_explosion_" + row + "_" + col
                            , "magic_volcano", "empty");
                
                explosions[row][col].destroy();
            }
        }
    }

    @Override
    public void fixedUpdate() {
        super.fixedUpdate();
        
        for (int row = 0; row < MAX_EXPLOSIONS_Y; row++) {
            for (int col = 0; col < MAX_EXPLOSIONS_X; col++) {
                Projectile explosion = explosions[row][col];
                if (explosion.isDestroyed() 
                        && Util.getTime() >= explosionsStartTime[row][col]) {
                    
                    String size = "";
                    switch (type) {
                        case VOLCANO_A -> size = "small";
                        case VOLCANO_B -> 
                            size = (Util.random(2) == 0) ? "small" : "medium";

                        case VOLCANO_C -> size = "medium";
                    }
                    ProjectileState projectileState = 
                            (ProjectileState) explosion.getStateManager()
                                                        .getState("animating");
                    
                    projectileState.setStateAnimationId("explosion_" + size);
                    explosion.spawn(owner, direction
                            , owner.getWx(), owner.getWy(), owner.getWz());
                    
                    explosion.setWx(Camera.getX() + col * CELL_WIDTH 
                            + Util.random(-CELL_WIDTH / 4, CELL_WIDTH / 4) 
                                + CELL_WIDTH / 2);
                    
                    int magicPathZ = 
                            Terrain.getMagicPathZ((int) explosion.getWx());

                    explosion.setWz(magicPathZ + row * CELL_HEIGHT 
                            + Util.random(-CANVAS_HEIGHT / 4, CANVAS_HEIGHT / 4) 
                                + CELL_HEIGHT / 2 - CANVAS_HEIGHT / 2);
                    
                    explosionsStartTime[row][col] = 
                            Util.getTime() + 500 + Util.random(500);
                }
            }
        }
    }
    
    @Override
    public void use() {
        destroyed = false;
        stage.addActor(this);
        Audio.playSound("magic_volcano_abc");
        finishedTime = Util.getTime() + 4000;
        for (int row = 0; row < MAX_EXPLOSIONS_Y; row++) {
            for (int col = 0; col < MAX_EXPLOSIONS_X; col++) {
                explosionsStartTime[row][col] = 
                        Util.getTime() + Util.random(1000);
            }
        }
    }

}
