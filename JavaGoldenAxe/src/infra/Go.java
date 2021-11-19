package infra;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Go class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Go {
    
    private static final BufferedImage IMAGE;
    
    private static long startTime;
    private static long nextTime;
    private static boolean enabled;
    private static boolean visible;
    private static int blinkCount;
    private static int desiredBlinkCount;
    
    static {
        IMAGE = Resource.getImage("go");
    }
    
    public static void update() {
        long currentTime = System.currentTimeMillis();
        if (enabled && currentTime >= startTime && currentTime >= nextTime) {
            visible = !visible;
            if (!visible && blinkCount >= desiredBlinkCount) {
                enabled = false;
            }
            else if (visible) {
                Audio.playSound("go");
                blinkCount++;
            }
            nextTime = System.currentTimeMillis() + 150;
        } 
    }
    
    public static void draw(Graphics2D g) {
        if (visible) {
            g.drawImage(IMAGE, 193, 42, null);
        }
    }

    public static void show(int desiredBlinkCount, long time) {
        Go.desiredBlinkCount = desiredBlinkCount;
        enabled = true;
        visible = false;
        startTime = System.currentTimeMillis() + time;
        nextTime = 0;
        blinkCount = 0;
    }
    
}
