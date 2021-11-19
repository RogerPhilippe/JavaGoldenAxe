package infra;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Shadow class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Shadow {
    
    private static final int SHADOW_INTENSITY = 128;
    private static final int SHADOW_MAX_Y = 18;
    
    private static class ShadowPixel {
        Point point;
        boolean hasRightVerticalShadow;
        boolean hasDownVerticalShadow;
        
        public ShadowPixel(int x, int y, boolean hasRightVerticalShadow
                                            , boolean hasDownVerticalShadow) {
            
            point = new Point(x, y);
            this.hasRightVerticalShadow = hasRightVerticalShadow;
            this.hasDownVerticalShadow = hasDownVerticalShadow;
        }
    }
    
    private final Sprite shadowSprite;
    private final List<ShadowPixel> pixels = new ArrayList<>();
    
    public Shadow(Sprite sprite) {
        shadowSprite = sprite.createSilhouette(Color.BLACK, true, 0.5);
        cacheShadowPixels(shadowSprite);
    }
    
    private void cacheShadowPixels(Sprite sprite) {
        BufferedImage shadowImage = sprite.getImage();
        int shadowHeight = shadowImage.getHeight();
        int shadowWidth = shadowImage.getWidth();
        int shadowColor = Color.BLACK.getRGB();
        for (int spriteY = 0; spriteY < shadowHeight; spriteY++) {
            for (int spriteX = 0; spriteX < shadowWidth; spriteX++) {
                if (shadowImage.getRGB(spriteX, spriteY) == shadowColor) {
                    boolean hasRightVerticalShadow = spriteX > 0 
                            && shadowImage.getRGB(spriteX - 1, spriteY) 
                                                            == shadowColor;
                    
                    boolean hasDownVerticalShadow = spriteY > 0 
                            && shadowImage.getRGB(spriteX, spriteY - 1) 
                                                            == shadowColor;
                    
                    pixels.add(new ShadowPixel(spriteX, spriteY
                            , hasRightVerticalShadow, hasDownVerticalShadow));
                }
            }
        }        
    }
    
    public void draw(Offscreen offscreen, double wx, double wy, double wz
            , double cameraX, double cameraY, Direction direction
                , double jumpHeight, double shear) {

//        Direction direction = shadowSprite.isFlipDirection()
//                    ? directionTmp.getOpposite() : directionTmp;

        int clipY = (int) (wy + wz - cameraY - SHADOW_MAX_Y);

        BufferedImage shadowImage = shadowSprite.getImage();
        int shadowWidth = shadowImage.getWidth();
        int shadowCenterX = shadowSprite.getCenterX();
        int shadowCenterY = 
                (int) (shadowSprite.getCenterY() - (jumpHeight * 0.2));
        
        pixels.forEach(pixel -> {
            Point point = pixel.point;
            double pixelWX = point.x + wx - shadowCenterX + shear * point.y;
            if (direction != shadowSprite.getOriginalDirection()) {
                pixelWX = (shadowWidth - point.x) + wx 
                            - (shadowWidth - shadowCenterX) + shear * point.y;
            }
            
            double pixelWY = point.y + wz - shadowCenterY;
            int terrainHeight 
                    = Terrain.getHeight((int) pixelWX, (int) pixelWY);

            // draw only the shadow pixels that are below to the player
            if (terrainHeight < wy - SHADOW_MAX_Y) {
                return;
            }
            
            int px = (int) (pixelWX - cameraX);
            int py = (int) (pixelWY + terrainHeight - cameraY);
            
            // render shadow pixel only if drawable
            if (Terrain.isFloorShadowDrawable((int) pixelWX, (int) pixelWY)) {
                offscreen.setPixel(px, py, 0, SHADOW_INTENSITY);
            }
            
            // draw small right vertical dither shadow if necessary
            if (pixel.hasRightVerticalShadow                     
                    && Terrain.isVerticalShadowDrawable(
                            (int) pixelWX, (int) pixelWY)) {

                int terrainHeightRight 
                        = Terrain.getHeight((int) (pixelWX - 1), (int) pixelWY);

                if (terrainHeight != terrainHeightRight) {
                    int pyRight = (int) (point.y + wz - shadowCenterY
                                + terrainHeightRight - cameraY);
                    
                    drawSmallVerticalCheckerBoardLine(
                            offscreen, px, py, pyRight, clipY);
                }
            }
            
            // draw small down vertical dither shadow if necessary
            if (pixel.hasDownVerticalShadow 
                    && Terrain.isVerticalShadowDrawable(
                            (int) pixelWX, (int) pixelWY)) {
                
                int terrainHeightDown 
                        = Terrain.getHeight((int) (pixelWX), (int) pixelWY - 1);

                if (terrainHeight != terrainHeightDown) {
                    int pyDown = (int) (point.y + wz - shadowCenterY
                            + terrainHeightDown - cameraY);
                    
                    drawSmallVerticalCheckerBoardLine(
                            offscreen, px, py, pyDown, clipY);
                }
            }
        });
    }

    private void drawSmallVerticalCheckerBoardLine(
                Offscreen offscreen, int x, int y1, int y2, int clipY) {
        
        if (y1 > y2) {
            int tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        int center = (y1 + y2) / 2;
        for (int y = y1; y <= center; y++) {
            if (y >= clipY && y < y1 + 2 && (x + y) % 2 == 0) {
                offscreen.setPixel(x, y, 0, SHADOW_INTENSITY);
            }
        }
        for (int y = y2; y > center; y--) {
            if (y >= clipY && y > y2 - 3 && (x + y) % 2 == 0) {
                offscreen.setPixel(x, y, 0, SHADOW_INTENSITY);
            }
        }
    }
    
}
