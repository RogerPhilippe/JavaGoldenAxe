package infra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Offscreen class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Offscreen {
    
    private final BufferedImage image;
    private final Graphics2D g2d;
    private final int[] data;
    private final int width;
    private final int height;

    public Offscreen(int width, int height) {
        this.width = width;
        this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2d = (Graphics2D) image.getGraphics();
        g2d.setBackground(new Color(0, 0, 0, 0));
        data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    }

    public BufferedImage getImage() {
        return image;
    }

    public Graphics2D getG2d() {
        return g2d;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void clear() {
        g2d.clearRect(0, 0, width, height);
    }
    
    public int getPixel(int x, int y) {
        if (x < 0 || y < 0 || x > width - 1 || y > height - 1) {
            return 0;
        }
        return data[x + y * width];
    }
    
    public void setPixel(int x, int y, int c) {
        setPixel(x, y, c, 255);
    }

    // alpha=0~255
    public void setPixel(int x, int y, int c, int alpha) {
        if (x < 0 || y < 0 || x > width - 1 || y > height - 1) {
            return;
        }
        data[x + y * width] = (alpha << 24) + (c & 0xffffff);
    }
    
}
