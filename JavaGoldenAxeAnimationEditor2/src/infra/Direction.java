package infra;

/**
 * Direction enum class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public enum Direction {
    
    RIGHT(1, 0), DOWN(0, 1), LEFT(-1, 0), UP(0, -1), IDLE(0, 0);
    
    private final int dx;
    private final int dy;

    private Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }
        
    public Direction getOpposite() {
        switch (this) {
            case LEFT : return Direction.RIGHT;
            case RIGHT : return Direction.LEFT;
            case UP : return Direction.DOWN;
            case DOWN : return Direction.UP;
            default : return Direction.IDLE;
        }        
    }
    
    public static Direction getRandom() {
        return Direction.values()[(int) (Math.random() * 4)];
    }
    
}
