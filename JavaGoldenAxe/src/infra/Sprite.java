package infra;

import static infra.Direction.*;
import static infra.Settings.*;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.CompositeContext;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Sprite class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Sprite {
    
    private static final Composite ADDITIVE_BLEND = new AdditiveComposite(1.0);

    private static class AdditiveComposite implements Composite {

        final double intensity;

        public AdditiveComposite(double intensity) {
            super();
            this.intensity = intensity;
        }

        @Override
        public CompositeContext createContext(ColorModel srcColorModel
                        , ColorModel dstColorModel, RenderingHints hints) {
            
            return new AdditiveCompositeContext(intensity);
        }

    }

    private static class AdditiveCompositeContext implements CompositeContext {

        final double intensity;

        public AdditiveCompositeContext(double intensity) {
            this.intensity = intensity;
        }

        final int[] pxSrc = new int[4];
        final int[] pxDst = new int[4];
        
        @Override
        public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
            int minChannels = Math.min(src.getNumBands(), dstIn.getNumBands());
            for (int x = 0; x < dstIn.getWidth(); x++) {
                for (int y = 0; y < dstIn.getHeight(); y++) {
                    src.getPixel(x, y, pxSrc);
                    dstIn.getPixel(x, y, pxDst);
                    double alpha = 255.0;
                    if (pxSrc.length > 3) {
                        alpha = pxSrc[3] / 255.0;
                    }
                    for (int i = 0; i < 3 && i < minChannels; i++) {
                        pxDst[i] = (int) Math.min(255
                                , pxSrc[i] * intensity * alpha + pxDst[i]);
                        
                        dstOut.setPixel(x, y, pxDst);
                    }
                }
            }
        }

        @Override
        public void dispose() {
        }
        
    }
        
    private final String id;
    private final BufferedImage image;
    private final int centerX;
    private final int centerY;
    private final Direction originalDirection;
    private final boolean useAdditiveblend;
    //private final boolean flipDirection;
    private final String hash;
    
    public Sprite(String id, BufferedImage image, int cx, int cy
                , Direction originalDirection //, boolean flipDirection
                    , boolean useAdditiveblend, String hash) {
        
        this.id = id;
        this.image = image;
        this.centerX = cx;
        this.centerY = cy;
        this.originalDirection = originalDirection; 
        //this.flipDirection = flipDirection;
        this.useAdditiveblend = useAdditiveblend;
        this.hash = hash;
    }

    public static String createHash(String id, String spriteSheetId
            , int x1, int y1, int w1, int h1
                , int cx, int cy, Direction originalDirection
                    , boolean useAdditiveblend) {
        
        return id + "_" + spriteSheetId + "_" + x1 + "_" + y1 + "_" 
                + w1 + "_" + h1 + "_" + cx + "_" + cy + "_" + originalDirection 
                    + "_" + useAdditiveblend;
    }
    
    public Sprite createSilhouette(Color color
                    , boolean flipVertically, double scaleY) {
        
        int silhouetteImageHeight = (int) (scaleY * image.getHeight());
        BufferedImage silhouetteImage = new BufferedImage(image.getWidth()
                        , silhouetteImageHeight, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = (Graphics2D) silhouetteImage.getGraphics();
        int silhouetteCenterX = centerX;
        int silhouetteCenterY = centerY;
        if (flipVertically) {
            silhouetteCenterY = (int) (scaleY * (image.getHeight() - centerY));
            g.drawImage(image, 0, silhouetteImageHeight
                    , image.getWidth(), -silhouetteImageHeight, null);
        }
        else {
            g.drawImage(image, 0, 0
                    , image.getWidth(), silhouetteImageHeight, null);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
        g.setColor(color);
        g.fillRect(0, 0
                , silhouetteImage.getWidth(), silhouetteImage.getHeight());
        
        return new Sprite(id + "_silhouette", silhouetteImage
                , silhouetteCenterX, silhouetteCenterY
                    , originalDirection, false, "");
    }
    
    public String getId() {
        return id;
    }

    public BufferedImage getImage() {
        return image;
    }

    public int getCenterX() {
        return centerX;
    }

    public int getCenterY() {
        return centerY;
    }

    public Direction getOriginalDirection() {
        return originalDirection;
    }

//    public boolean isFlipDirection() {
//        return flipDirection;
//    }

    public String getHash() {
        return hash;
    }
    
//    public void draw(Graphics2D g, double wx, double wy, double wz;
//            , int cameraX, int cameraY, Direction directionTmp) {
//        
//        draw(g, wx, wy, wz, cameraX, cameraY, directionTmp, 1.0, 1.0, 0.0);
//    }

    private final AffineTransform transform = new AffineTransform();
    
    public void draw(Graphics2D g, double wx, double wy, double wz
            , int cameraX, int cameraY, Direction direction
                , double scaleX, double scaleY, double angle) {
        
        direction = direction != originalDirection ? LEFT : RIGHT;
        
        
        Composite originalComposite = null;
        if (useAdditiveblend) {
            originalComposite = g.getComposite();
            g.setComposite(ADDITIVE_BLEND);
        }

        transform.setToIdentity();
        transform.translate(wx - cameraX, wy + wz - cameraY);
        transform.rotate(angle);
        transform.scale(direction.getDx() * scaleX, scaleY);
        transform.translate(-centerX, -centerY);
        g.drawImage(image, transform, null);
        
        if (useAdditiveblend) {
            g.setComposite(originalComposite);
        }

        if (SHOW_DEBUG_SPRITE_BOUNDING_BOX) {
            int spriteHeight = image.getHeight();
            int spriteWidth = direction.getDx() * image.getWidth();
            int spriteY = (int) (wy + wz - centerY - cameraY);
            int spriteX = (int) (wx - direction.getDx() * centerX - cameraX);
            g.setColor(DEBUG_SPRITE_BOUNDING_BOX_COLOR);
            spriteX += spriteWidth < 0 ? spriteWidth : 0;
            g.drawRect(spriteX, spriteY, image.getWidth(), spriteHeight);
        }
            
        if (SHOW_DEBUG_SPRITE_CENTER) {
            g.setColor(DEBUG_SPRITE_CENTER_COLOR);
            double px = wx - cameraX;
            double py = wy + wz - cameraY;
            g.drawLine((int) (px - 1), (int) py, (int) (px + 1), (int) py);
            g.drawLine((int) px, (int) (py - 1), (int) px, (int) (py + 1));
        }
    }

    @Override
    public String toString() {
        return "Sprite{" + "id=" + id 
            + ", centerX=" + centerX + ", centerY=" + centerY 
                + ", originalDirection=" + originalDirection + '}';
    }
    
}
