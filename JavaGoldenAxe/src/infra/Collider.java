package infra;

import static infra.Collider.Type.BODY;
import java.awt.Rectangle;

/**
 * Collider class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Collider extends Rectangle {
    
    public enum Type { BODY, ATTACK, TOUCH, FIRE }
    
    // collision order:
    // body or attack <-> touch  -> reaction (triggered when touched)
    //           body <-> fire   -> reaction (triggered when touched + fire key)
    //           body <-> attack -> reaction (triggered when touched)
    public enum Reaction { NONE, STUNNED_1, STUNNED_2, KNOCK_DOWN
                            , ATTACK_SHORT, ATTACK_LONG
                                , GRAB_AND_THROW, PICK_UP, MOUNT }
    
    private Type type = BODY;
    private Reaction reaction;
    private final Direction originalDirection;
    private int damage;
    private int score;
    
    public Collider(int x, int y, int width, int height, int damage, int score) {
        this(x, y, width, height, null, damage, score);
    }

    public Collider(int x, int y, int width, int height
            , Direction originalDirection, int damage, int score) {
        
        super(x, y, width, height);
        this.originalDirection = originalDirection;
        this.damage = damage;
        this.score = score;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Reaction getReaction() {
        return reaction;
    }

    public void setReaction(Reaction reaction) {
        this.reaction = reaction;
    }

    public int getDamage() {
        return damage;
    }

    public int getScore() {
        return score;
    }

    public void convertToCameraSpace(Collider convertedCollider
                    , double wx, double wy, double wz
                    , double cameraX, double cameraY, Direction direction) {
        
        int xTmp = x;
        if (direction != originalDirection) {
            xTmp = -width - x + 1;
        }
        int cameraSpaceX = (int) (xTmp + wx - cameraX);
        int cameraSpaceY = (int) (y + wy + wz - cameraY);
        convertedCollider.setRect(cameraSpaceX, cameraSpaceY, width, height);
        convertedCollider.setType(convertedCollider.type);
        convertedCollider.setReaction(convertedCollider.reaction);
    }

    @Override
    public String toString() {
        return "Collider{" + "type=" + type + ", reaction=" + reaction 
                + ", originalDirection=" + originalDirection 
                    + ", damage=" + damage + ", score=" + score + '}';
    }

    
}
