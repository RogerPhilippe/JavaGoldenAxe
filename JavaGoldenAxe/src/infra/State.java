package infra;

import java.awt.Graphics2D;

/**
 * State class.
 * 
 * @param <T> owner
 * @param <S> state manager

* @author Leonardo Ono (ono.leo80@gmail.com)
 */
public abstract class State<T, S> {

    protected final S manager;
    protected final String name;
    protected final T owner;

    public State(S manager, String name, T owner) {
        this.manager = manager;
        this.name = name;
        this.owner = owner;
    }
    
    public void start() {
        // implement your code here
    }

    public S getManager() {
        return manager;
    }
    
    public String getName() {
        return name;
    }

    public T getOwner() {
        return owner;
    }
    
    public void onEnter() {
        // implement your code here
    }

    public void onExit() {
        // implement your code here
    }

    public void update(long delta) {
        // implement your code here
    }
    
    public void fixedUpdate() {
        // implement your code here
    }
    
    public void draw(Graphics2D g) {
        // implement your code here
    }
    
    public void draw(Graphics2D g, double wx, double wy, double wz
            , int cameraX, int cameraY, Direction direction, boolean blink
                , double scaleX, double scaleY, double angle) {
        
        // implement your code here
    }

}
