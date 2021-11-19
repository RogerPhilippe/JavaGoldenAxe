package infra;

import static infra.Settings.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Animation class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Animation {

    public static class FramePoint extends Point {
        String name;
        Sprite sprite;
        
        public FramePoint(String name, Sprite sprite, int x, int y) {
            super(x, y);
            this.name = name;
            this.sprite = sprite;
        }

        public String getName() {
            return name;
        }

        public Sprite getSprite() {
            return sprite;
        }

        public void convertPointToCameraSpace(Point convertedPoint
                , double wx, double wy, double wz
                    , double cameraX, double cameraY, Direction direction) {

            double xTmp = this.x;
            if (direction != sprite.getOriginalDirection()) {
                xTmp = -xTmp;
            }
            int cameraSpaceX = (int) (xTmp + wx - cameraX);
            int cameraSpaceY = (int) (this.y + wy + wz - cameraY);
            convertedPoint.setLocation(cameraSpaceX, cameraSpaceY);
        }            
    }
    
    public static class Frame {
        List<Collider> colliders;
        Shadow shadow;
        Sprite[] sprites = new Sprite[3];
        Map<String, FramePoint> points = new HashMap<>();
        
        public Frame(Sprite sprite, boolean createShadow
                , boolean createBlink, Color blinkColor1, Color blinkColor2) {
            
            this.colliders = new ArrayList<>();
            if (createShadow) {
                this.shadow = new Shadow(sprite);
            }
            this.sprites[0] = sprite;
            // blink sprites for hit by magic effect for enemies
            if (createBlink) {
                this.sprites[1] = sprite.createSilhouette(blinkColor1, false, 1.0);
                this.sprites[2] = sprite.createSilhouette(blinkColor2, false, 1.0);
            }
        }
        
        public Sprite getSprite(int index) {
            return sprites[index];
        }
        
        public Map<String, FramePoint> getPoints() {
            return points;
        }
        
        public FramePoint getPoint(String pointName) {
            return points.get(pointName);
        }
        
        public FramePoint getFirstPoint(String startsWith) {
            String key = null;
            for (String keyCandidate : points.keySet()) {
                if (keyCandidate.startsWith(startsWith)) {
                    key = keyCandidate;
                    break;
                }
            }
            return points.get(key);
        }
        
        public static String createHash(Sprite sprite, boolean createShadow
                , boolean createBlink, Color blinkColor1, Color blinkColor2) {
            
            return sprite.getHash() + "_" + createShadow + "_" 
                    + createBlink + "_" + blinkColor1 + "_" + blinkColor2;
        }
        
    }
    
    private final String name;
    private final List<Frame> frames = new ArrayList<>();
    private final List<Actor> reactedActors = new ArrayList<>();
    private final double blinkStartValue = 9999 * Math.random();
    private final boolean shadowVisible;
    private double speed = 0.1;
    private double currentFrameIndex;
    private boolean looping = false;
    private boolean collisionCheckEnabled = true;
    private double blinkTimeFactor = 0.00000005;
    private boolean finished;
    private final Map<String, String> parameters = new HashMap<>();
    
    public Animation(String name, boolean shadowVisible) {
        this.name = name;
        this.shadowVisible = shadowVisible;
    }

    public String getName() {
        return name;
    }
    
    public void addFrame(Sprite sprite, Rectangle characterRectangle
                        , List<Collider> characterColliders
                            , List<FramePoint> characterPoints
                                , boolean createShadow, boolean createBlink
                                    , Color blinkColor1, Color blinkColor2) {
        
        Frame frame = Resource.getFrame(
                sprite, createShadow, createBlink, blinkColor1, blinkColor2);

        // colliders
        for (Collider collider : characterColliders) {
            if (collider.intersects(characterRectangle)) {
                Collider copy = new Collider(collider.x, collider.y
                    , collider.width, collider.height
                        , sprite.getOriginalDirection()
                            , collider.getDamage(), collider.getScore());
                
                copy.setType(collider.getType());
                copy.setReaction(collider.getReaction());
                
                copy.x -= characterRectangle.x + sprite.getCenterX();
                copy.y -= characterRectangle.y + sprite.getCenterY();
                frame.colliders.add(copy);
            }
        }
        
        // points
        for (FramePoint point : characterPoints) {
            if (characterRectangle.contains(point)) {
                FramePoint copy = new FramePoint(
                        point.getName(), sprite, point.x, point.y);
                
                copy.x -= characterRectangle.x + sprite.getCenterX();
                copy.y -= characterRectangle.y + sprite.getCenterY();
                frame.points.put(point.getName(), copy);
            }
        }

        frames.add(frame);
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }
    
    public int getFramesCount() {
        return frames.size();
    }
    
    public Frame getFrame(int index) {
        return frames.get(index);
    }
    
    public Frame getCurrentFrame() {
        return frames.get((int) currentFrameIndex);
    } 
    
    public double getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    public void setCurrentFrameIndex(double currentFrameIndex) {
        this.finished = false;
        this.currentFrameIndex = currentFrameIndex;
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }

    public boolean isCollisionCheckEnabled() {
        return collisionCheckEnabled;
    }

    public void setCollisionCheckEnabled(boolean collisionCheckEnabled) {
        this.collisionCheckEnabled = collisionCheckEnabled;
    }

    public void setBlinkTimeFactor(double blinkTimeFactor) {
        this.blinkTimeFactor = blinkTimeFactor;
    }

    public void clearReactedActors() {
        reactedActors.clear();
    }
    
    public void addReactedActor(Actor actor) {
        reactedActors.add(actor);
    }

    public boolean isActorReacted(Actor actor) {
        return reactedActors.contains(actor);
    }
    
    public void setParameter(String key, String value) {
        switch (key) {
            case "animation_speed" -> speed = Double.parseDouble(value);
            case "animation_loop" -> looping = Boolean.parseBoolean(value);
            default -> parameters.put(key, value);
        }
    }
    
    public boolean isParameterAvailable(String key) {
        return parameters.get(key) != null;
    }
    
    public String getParameter(String key) {
        String value;
        switch (key) {
            case "animation_speed" -> value = "" + speed;
            case "animation_loop" -> value = "" + looping;
            default -> value = parameters.get(key);
        }
        return value;
    }

    public boolean isFinished() {
        return finished;
    }

    public void update() {
        finished = false;
        currentFrameIndex += speed;
        if (currentFrameIndex > frames.size() - 0.01) {
            if (looping) {
                currentFrameIndex = 0.0;
                finished = false;
            }
            else {
                currentFrameIndex = frames.size() - 0.01;
                finished = true;
            }
        }
    }

    public void drawShadow(Offscreen offscreen, double wx, double wy, double wz
                , double cameraX, double cameraY, Direction direction
                    , double jumpHeight, double shear) {
        
        Frame currentFrame = frames.get((int) currentFrameIndex);
        if (shadowVisible) {
            currentFrame.shadow.draw(offscreen, wx, wy, wz
                        , cameraX, cameraY, direction, jumpHeight, shear);
        }
    }

    private final Collider colliderTmp = new Collider(0, 0, 0, 0, 0, 0);
    private final Point pointTmp = new Point();
    
    public void draw(Graphics2D g, double wx, double wy, double wz
            , int cameraX, int cameraY, Direction direction, boolean blink
                , double scaleX, double scaleY, double angle) {
        
        Frame currentFrame = frames.get((int) currentFrameIndex);
        int blinkIndex = 0;
        if (blink) {
            blinkIndex = (int) (System.nanoTime() * blinkTimeFactor
                        + blinkStartValue) % currentFrame.sprites.length;
        }
        
        Sprite sprite = currentFrame.sprites[blinkIndex];
        sprite.draw(g, wx, wy, wz, cameraX, cameraY, direction
                                                , scaleX, scaleY, angle);

        if (SHOW_DEBUG_COLLIDERS) {
            for (Collider collider : currentFrame.colliders) {
                collider.convertToCameraSpace(
                        colliderTmp, wx, wy, wz, cameraX, cameraY, direction);
                
                switch (collider.getType()) {
                    case BODY -> g.setColor(DEBUG_COLLIDERS_BODY_COLOR);
                    case ATTACK -> g.setColor(DEBUG_COLLIDERS_ATTACK_COLOR);
                    case TOUCH -> g.setColor(DEBUG_COLLIDERS_TOUCH_COLOR);
                    case FIRE -> g.setColor(DEBUG_COLLIDERS_FIRE_COLOR);
                }
                g.fill(colliderTmp);
            }
        }
        
        if (SHOW_DEBUG_POINTS) {
            g.setColor(DEBUG_POINTS_COLOR);
            for (FramePoint point : currentFrame.points.values()) {
                point.convertPointToCameraSpace(
                        pointTmp, wx, wy, wz, cameraX, cameraY, direction);
                
                g.fillOval(pointTmp.x, pointTmp.y, 1, 1);
            }
        }
    }
    
}
