package infra;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Input class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Input implements KeyListener {
    
    private static final Set<Integer> KEYS_PRESSED = new HashSet<>();
    private static final Set<Integer> KEYS_PRESSED_CONSUMED = new HashSet<>();
    private static final List<KeyListener> LISTENERS = new ArrayList<>();
    
    private static final long DOUBLE_PRESSED_TIME = 250;
    private static final Map<Integer, long[]> KEYS_DOUBLE_PRESSED 
                                                            = new HashMap<>();

    public static void addListener(KeyListener listener) {
        Input.LISTENERS.add(listener);
    }

    public static synchronized boolean isKeyPressed(int keyCode) {
        return KEYS_PRESSED.contains(keyCode);
    }

    public static synchronized boolean isKeyDoublePressed(int keyCode) {
        boolean result = false;
        long[] lastTimes = KEYS_DOUBLE_PRESSED.get(keyCode);
        if (lastTimes != null) {
            if (lastTimes[0] > 0 && lastTimes[1] > 0) {
                result = Util.getTime() - lastTimes[1] <= DOUBLE_PRESSED_TIME
                            && KEYS_PRESSED.contains(keyCode);

                if (result) {
                    lastTimes[0] = -1;
                    lastTimes[1] = -1;
                }
            }
            else {
                result = false;
            }
        }
        return result;
    }

    public static synchronized boolean isKeyJustPressed(int keyCode) {
        if (!KEYS_PRESSED_CONSUMED.contains(keyCode) 
                && KEYS_PRESSED.contains(keyCode)) {
            
            KEYS_PRESSED_CONSUMED.add(keyCode);
            return true;
        }
        return false;
    }
    
    @Override
    public synchronized void keyTyped(KeyEvent e) {
        if (LISTENERS != null) {
            LISTENERS.forEach(action -> action.keyTyped(e));
        }
    }

    @Override
    public synchronized void keyPressed(KeyEvent e) {
        if (!KEYS_PRESSED.contains(e.getKeyCode())) {
            long[] lastTimes = KEYS_DOUBLE_PRESSED.get(e.getKeyCode());
            if (lastTimes == null) {
                lastTimes = new long[2];
                KEYS_DOUBLE_PRESSED.put(e.getKeyCode(), lastTimes);
            }
            if (lastTimes[0] < 0 
                    || Util.getTime() - lastTimes[0] > DOUBLE_PRESSED_TIME) {
                
                lastTimes[0] = Util.getTime();
                lastTimes[1] = -1;
            }
            //System.out.println("keyPressed double key " + e.getKeyCode());
        }

        KEYS_PRESSED.add(e.getKeyCode());
        if (LISTENERS != null) {
            LISTENERS.forEach(action -> action.keyPressed(e));
        }
    }
    
    @Override
    public synchronized void keyReleased(KeyEvent e) {
        KEYS_PRESSED.remove(e.getKeyCode());
        
        long[] lastTimes = KEYS_DOUBLE_PRESSED.get(e.getKeyCode());
        if (lastTimes != null) {
            if (Util.getTime() - lastTimes[0] <= DOUBLE_PRESSED_TIME) {
                lastTimes[1] = Util.getTime();
            }
            else {
                lastTimes[0] = -1;
                lastTimes[1] = -1;
            }
            // System.out.println("keyReleased double key " + e.getKeyCode());
        }
        
        KEYS_PRESSED_CONSUMED.remove(e.getKeyCode());
        if (LISTENERS != null) {
            LISTENERS.forEach(action -> action.keyReleased(e));
        }
    }
    
}
