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
        return switch (this) {
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            default -> Direction.IDLE;
        };        
    }
    
    public static Direction getRandom() {
        return Direction.values()[(int) (Math.random() * 4)];
    }
    
}
