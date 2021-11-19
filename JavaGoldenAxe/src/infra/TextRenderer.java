package infra;

import static infra.Settings.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Text renderer class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class TextRenderer {

    public static final Color DEFAULT_FONT_COLOR = Util.getColor("0xceefef");
    public static final Color DEFAULT_FONT_SHADOW_COLOR 
                                                = Util.getColor("0x8c0000");

    private static final Font FONT;
    
    static {
        FONT = Resource.getFont(RES_DEFAULT_FONT).deriveFont(7.0f);
    }
    
    public static void draw(Graphics2D g, String text, int col, int row) {
        draw(g, text, col, row, DEFAULT_FONT_COLOR, DEFAULT_FONT_SHADOW_COLOR);
    }
    
    public static void draw(
            Graphics2D g, String text, int col, int row, Color color) {
        
        g.setFont(FONT);
        g.setColor(color);
        g.drawString(text, col * 8, (row + 1) * 10);
    }

    public static void draw(Graphics2D g
            , String text, int col, int row, Color color, Color colorShadow) {
        
        g.setFont(FONT);
        g.setColor(colorShadow);
        g.drawString(text, col * 8 + 0, (row + 1) * 10 + 0);
        g.drawString(text, col * 8 + 1, (row + 1) * 10 + 0);
        g.drawString(text, col * 8 + 0, (row + 1) * 10 + 1);
        g.drawString(text, col * 8 + 1, (row + 1) * 10 + 1);
        g.setColor(color);
        g.drawString(text, col * 8, (row + 1) * 10);
    }
    
}
