package infra;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 * StateManager class.
 * 
 * @param <T> owner
 * @param <S> state manager
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class StateManager<T, S> {
    
    protected final Map<String, State<T, S>> states = new HashMap<>();
    protected State<T, S> currentState;
    protected State<T, S> nextState;

    public StateManager() {
    }

    public State<T, S> getCurrentState() {
        return currentState;
    }
    
    public void addState(State state) {
        states.put(state.getName(), state);
    }
    
    public void removeState(String stateName) {
        states.remove(stateName);
    }

    public State getState(String stateId) {
        return states.get(stateId);
    }

    public void startAll() {
        states.keySet().forEach(key -> states.get(key).start());
    }
    
    public boolean isStateAvailable(String stateName) {
        return states.containsKey(stateName);
    }

    public State<T, S> switchTo(String stateName) { 
        if (currentState == null) {
            currentState = states.get(stateName);
            currentState.onEnter();
            return currentState;
        }
        else {
            nextState = states.get(stateName);
            return nextState;
        }
    }
    
    public void fixedUpdate() {
        if (nextState != null) {
            if (currentState != null) {
                currentState.onExit();
            }
            currentState = nextState;
            nextState = null;
            currentState.onEnter();
        }
        if (currentState != null) {
            currentState.fixedUpdate();
        }
    }

    public void draw(Graphics2D g) {
        if (currentState != null) {
            currentState.draw(g);
        }
    }

    public void draw(Graphics2D g, double wx, double wy, double wz
            , int cameraX, int cameraY, Direction direction, boolean blink
                , double scaleX, double scaleY, double angle) {
        
        if (currentState != null) {
            currentState.draw(g, wx, wy, wz, cameraX, cameraY
                            , direction, blink, scaleX, scaleY, angle);
        }
    }

}
