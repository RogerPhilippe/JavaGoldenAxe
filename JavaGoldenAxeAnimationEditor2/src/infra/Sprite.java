package infra;


import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author admin
 */
public class Sprite {

    private final String id;
    private final BufferedImage image;
    private final Point origin;
    private Direction originalDirection;
    private final Rectangle rectangle;
    private Map<String, Point> points = new HashMap<>();
    
    public Sprite(String id, BufferedImage image, int ox, int oy
            , Direction originalDirection, Rectangle rectangle) {
        
        this.id = id;
        this.image = image;
        origin = new Point(ox, oy);
        this.originalDirection = originalDirection;
        this.rectangle = rectangle;
    }

    public String getId() {
        return id;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Point getOrigin() {
        return origin;
    }

    public Direction getOriginalDirection() {
        return originalDirection;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public void setOriginalDirection(Direction originalDirection) {
        this.originalDirection = originalDirection;
    }

    public void  addPoint(String name, int x, int y) {
        points.put(name, new Point(x, y));
    }
    
    public Point getPoint(String name) {
        return points.get(name);
    }
    
    public void draw(Graphics2D g, int x, int y) {
        g.drawImage(image, x - origin.x, y - origin.y, null);
    }

    @Override
    public String toString() {
        return "sprite " + id + " " + originalDirection + " " 
                + rectangle.x + " " + rectangle.y + " " + rectangle.width + " " 
                + rectangle.height + " " + origin.x + " " + origin.y;
    }
    
}
