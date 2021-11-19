package infra;

import java.awt.AlphaComposite;
import static java.awt.AlphaComposite.SRC_OVER;
import java.awt.Composite;

/**
 * Transparency class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Transparency {

    private static final Composite[] TRANSPARENT_COMPOSITES 
                                                        = new Composite[256];
    
    static {
        createTransparentComposites();
    }
    
    private static void createTransparentComposites() {
        for (int alphaInt = 0; alphaInt < 256; alphaInt++) {
            float alpha = alphaInt / 255.0f;
            TRANSPARENT_COMPOSITES[alphaInt] 
                    = AlphaComposite.getInstance(SRC_OVER, alpha);
        }
    }    

    public static Composite getComposite(int intensity) {
        return TRANSPARENT_COMPOSITES[intensity];
    }
    
}
