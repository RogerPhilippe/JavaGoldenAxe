package infra;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Util class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Util {
    
    private static final Random RANDOM = new Random(System.nanoTime());
    
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int random(int n) {
        return RANDOM.nextInt(n);
    }

    public static int random(int a, int b) {
        return a + RANDOM.nextInt(b - a + 1);
    }
    
    private static final Map<String, Color> COLORS = new HashMap<>();
    
    // example: red color = 0xff0000
    public static Color getColor(String encodedColor) {
        Color color = COLORS.get(encodedColor);
        if (color == null) {
            if (encodedColor.startsWith("0x") && encodedColor.length() == 10) {
                int r = Integer.parseUnsignedInt(
                            encodedColor.substring(2, 4), 16);
                
                int g = Integer.parseUnsignedInt(
                            encodedColor.substring(4, 6), 16);
                
                int b = Integer.parseUnsignedInt(
                            encodedColor.substring(6, 8), 16);
                
                int a = Integer.parseUnsignedInt(
                            encodedColor.substring(8, 10), 16);
                
                color = new Color(r, g, b, a);
                COLORS.put(encodedColor, color);
            }
            else {
                color = Color.decode(encodedColor);
                COLORS.put(encodedColor, color);
            }
        }
        return color;
    }

    public static long getTime() {
        return System.currentTimeMillis();
    }
    
}
