package infra;

import static infra.Settings.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Terrain class.
 * 
 * axis convention:
 * ----------------
 * left -x <-> +x right (horizontal)
 *   up -y <-> +y down (vertical / jump)  
 * next -z <-> +z far (depth)
 * 
 * 
 * color information bits:
 * -----------------------
 * color = v00f w000 hhhh_hhhh_hhhh_hhhh
 * 
 * h: terrain_height = (color & 0xffff) - 32768
 * w: is_terrain_walkable = (color & 0x080000) == 0x080000
 *
 * w: force_enemy_going_up = (color & 0x010000) == 0x010000
 * 
 * f: render_floor_shadow = (color & 0x100000) == 0x100000
 * v: render_vertical_shadow = (color & 0x800000) == 0x800000
 * 
 * follow_camera_y = (color & 0xffff) != 0xffff (65535d)
 * 
 * 
 * terrain -> screen space transformation:
 * ---------------------------------------
 * screen_x = player_x 
 * screen_y = player_y + player_z + terrain_height - camera_y
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Terrain {

    public static final int MAX_HEIGHT = 32767;
    
    private static final Map<String, Rectangle> BLOCKERS = new HashMap<>();
    
    private static BufferedImage collisionMask;
    private static BufferedImage debugHeightImage;
    private static int width;
    private static int height;
    public static int deathHeight = MAX_HEIGHT;
    
    private static int[] magicPathZ;
    
    public static int getMagicPathZ(int x) {
        if (magicPathZ == null || x < 0 || x > magicPathZ.length - 1) {
            return 0;
        }
        return magicPathZ[x];
    }

    public static void setMagicPathZ(String[] data) {
        int size = Integer.parseInt(data[data.length - 2]);
        magicPathZ = new int[size + 1];
        int x1 = Integer.parseInt(data[1]);
        int y1 = Integer.parseInt(data[2]);
        for (int i = 3; i < data.length; i += 2) {
            int x2 = Integer.parseInt(data[i]);
            int y2 = Integer.parseInt(data[i + 1]);
            double dx = x2 - x1;
            double dy = y2 - y1;
            double ny = 0.0;
            if (dx != 0) {
                ny = dy / dx;
            }
            for (int i2 = x1; i2 <= x2; i2++) {
                magicPathZ[i2] = (int) (y1 + ny * (i2 - x1));
            }
            x1 = x2;
            y1 = y2;
        }
    }
    
    public static BufferedImage getCollisionMask() {
        return collisionMask;
    }
    
    public static int getDeathHeight() {
        return deathHeight;
    }

    public static void setDeathHeight(int deathHeight) {
        Terrain.deathHeight = deathHeight;
    }

    public static void addBlocker(
            String blockerId, int wx, int wz, int width, int height) {
        
        BLOCKERS.put(blockerId, new Rectangle(wx, wz, width, height));
    }
    
    public static void removeBlocker(String blockerId) {
        BLOCKERS.remove(blockerId);
    }
    
    public static void reset() {
        collisionMask = null;
        debugHeightImage = null;
        width = 0;
        height = 0;
        deathHeight = MAX_HEIGHT;
        BLOCKERS.clear();
    }

    public static void setCollisionMask(BufferedImage collisionMask) {
        Terrain.collisionMask = collisionMask;
        width = collisionMask.getWidth();
        height = collisionMask.getHeight();
        if (SHOW_DEBUG_TERRAIN_HEIGHT) {
            drawDebugTerrainHeight();
        }
    }
    
    private static void drawDebugTerrainHeight() {
        int w = collisionMask.getWidth();
        int h = collisionMask.getHeight();
        debugHeightImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < debugHeightImage.getHeight(); y += 1) {
            for (int x = y % 2; x < debugHeightImage.getWidth(); x += 2) {
                int c = collisionMask.getRGB(x, y);
                if (isVerticalShadowDrawable(x, y)) {
                    try {
                        debugHeightImage.setRGB(x, y + getHeight(x, y), c);
                    }
                    catch (Exception e) { }
                }
            }
        }
    }
    
    public static boolean followCameraY(double wx, double wy) {
        int wxi = (int) wx;
        int wyi = (int) wy;
        if (wxi >= 0 && wx < width && wyi >= 0 && wyi < height) {
            return getHeight(wxi, wyi) != MAX_HEIGHT;
        }
        return false;
    }
    
    public static boolean forceEnemyGoingUp(double wx, double wy) {
        int wxi = (int) wx;
        int wyi = (int) wy;
        if (wxi >= 0 && wx < width && wyi >= 0 && wyi < height) {
            int c = collisionMask.getRGB(wxi, wyi);
            return (c & 0x010000) == 0x010000;
        }
        return false;
    }

    public static boolean isWalkable(double wx, double wy) {
        boolean walkable = false;
        int wxi = (int) wx;
        int wyi = (int) wy;
        if (wxi >= 0 && wx < width && wyi >= 0 && wyi < height) {
            int c = collisionMask.getRGB(wxi, wyi);
            walkable = (c & 0x080000) == 0x080000;
        }
        if (walkable) {
            for (Rectangle area : BLOCKERS.values()) {
                if (area.contains(wxi, wyi)) {
                    walkable = false;
                    break;
                }
            }
        }
        return walkable;
    }

    public static boolean isVerticalShadowDrawable(double wx, double wy) {
        int wxi = (int) wx;
        int wyi = (int) wy;
        if (wxi >= 0 && wx < width && wyi >= 0 && wyi < height) {
            int c = collisionMask.getRGB(wxi, wyi);
            return (c & 0x800000) == 0x800000;
        }
        return false;
    }

    public static boolean isFloorShadowDrawable(double wx, double wy) {
        int wxi = (int) wx;
        int wyi = (int) wy;
        if (wxi >= 0 && wx < width && wyi >= 0 && wyi < height) {
            int c = collisionMask.getRGB(wxi, wyi);
            return (c & 0x100000) == 0x100000;
        }
        return false;
    }
    
    public static int getWidth() {
        return width;
    }
    
    public static int getHeight(double wx, double wy) {
        int c = Color.BLACK.getRGB();
        int wxi = (int) wx;
        int wyi = (int) wy;
        if (wxi >= 0 && wx < width && wyi >= 0 && wyi < height) {
            c = collisionMask.getRGB(wxi, wyi);
        }
        int terrainHeight = (c & 0xffff) - (MAX_HEIGHT + 1);
        return terrainHeight;
    }

    public static double getMaxX() {
        return collisionMask.getWidth() - 1;
    }
    
    public static void drawDebugHeightImage(
                    Graphics2D g, int cameraX, int cameraY) {
        
        if (debugHeightImage != null) {
            g.drawImage(debugHeightImage, -cameraX, -cameraY, null);
        }
    }
    
}
