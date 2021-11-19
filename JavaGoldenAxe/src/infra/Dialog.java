package infra;

import static infra.Settings.*;
import static infra.TextRenderer.*;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Dialog class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Dialog {
    
    private static final Offscreen OFFSCREEN;
    
    private static boolean visible;
    
    private static int col;
    private static int row;
    private static int width;
    private static int height;
    private static int startCol;
    private static int startRow;
    private static boolean baloonVisible;
    
    private static String text;
    private static double textIndex;
    private static int lastTextIndex;
    private static double textSpeed;
    
    private static final Color BALOON_COLOR = Util.getColor("0x3c0000");
    private static final Color BALOON_BORDER_COLOR = Util.getColor("0xceefef");
    
    private static Color color;
    private static Color colorShadow;
    
    static {
        OFFSCREEN = new Offscreen(CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    public static void updateAnimator(
            double frameIndex, double startFrame, double endFrame) {
        
        if (!visible) {
            return;
        }
        double textIndexTmp = (text.length() - 1) 
                        * (frameIndex - startFrame) / (endFrame - startFrame);
        
        updateInternal(textIndexTmp);
    }
    
    
    public static void update() {
        if (!visible) {
            return;
        }
        updateInternal(textIndex + textSpeed);
    }
    
    private static void updateInternal(double textIndexTmp) {
        textIndex = textIndexTmp;
        if ((int) textIndex > text.length() - 1) {
            textIndex = text.length() - 1;
        }
        if ((int) textIndex > lastTextIndex) {
            for (int i = lastTextIndex + 1; i <= textIndex; i++) {
                char c = text.charAt(i);
                lastTextIndex = i;
                if (c == '|') {
                    col = startCol;
                    row++;
                    continue;
                }
                if (colorShadow == null) {
                    TextRenderer.draw(
                            OFFSCREEN.getG2d(), "" + c, col, row, color);
                }
                else {
                    TextRenderer.draw(OFFSCREEN.getG2d()
                            , "" + c, col, row, color, colorShadow);
                }
                col++;
            }
        }
        
    }
    
    public static void draw(Graphics2D g) {
        if (visible) {
            if (baloonVisible) {
                g.setColor(BALOON_BORDER_COLOR);
                g.fillRoundRect((startCol - 1) * 9 - 5, (startRow + 1) * 9 - 3
                                        , width * 9 + 6, height * 9 + 9, 8, 8);
                
                g.setColor(BALOON_COLOR);
                g.fillRoundRect((startCol - 1) * 9 - 4, (startRow + 1) * 9 - 2
                                        , width * 9 + 4, height * 9 + 7, 7, 7);
            }
            g.drawImage(OFFSCREEN.getImage(), 0, 0, null);
        }
    }

    public static void show(int startCol, int startRow, int width, int height
                                , String text, boolean baloonVisible) {
        
        show(startCol, startRow, width, height, text,baloonVisible
                        , DEFAULT_FONT_COLOR, DEFAULT_FONT_SHADOW_COLOR);
    }

    public static void show(int startCol, int startRow, int width, int height
                                , String text, boolean baloonVisible
                                    , Color color, Color colorShadow) {
        
        OFFSCREEN.clear();
        col = startCol;
        row = startRow;
        Dialog.width = width;
        Dialog.height = height;
        Dialog.baloonVisible = baloonVisible;
        Dialog.startCol = startCol;
        Dialog.startRow = startRow;
        Dialog.text = text;
        textSpeed = 0.2;
        textIndex = 0.0;
        lastTextIndex = -1;
        Dialog.color = color;
        Dialog.colorShadow = colorShadow;
        visible = true;
    }

    public static int getLastTextIndex() {
        return lastTextIndex;
    }

    public static boolean isVisible() {
        return visible;
    }
    
    public static void hide() {
        visible = false;
    }
    
    public static void showAll() {
        textIndex = text.length() - 1;
    }

    public static boolean isFinished() {
        return (int) textIndex == text.length() - 1;
    }

}
