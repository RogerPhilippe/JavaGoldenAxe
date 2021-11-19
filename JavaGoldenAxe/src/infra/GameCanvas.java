package infra;

import static infra.Settings.*;
import java.awt.Graphics2D;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferStrategy;

/**
 * GameCanvas class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class GameCanvas extends Canvas {
    
    private final Dimension preferredSize 
            = new Dimension(PREFERRED_SCREEN_WIDTH, PREFERRED_SCREEN_HEIGHT);

    private Offscreen offscreen;
    private final GoldenAxeGame goldenAxeGame;
    private BufferStrategy bs;
    private boolean running;
    private Thread gameLoopThread;
    private final Rectangle sizeWithAspectRatio = new Rectangle();
    
    public GameCanvas(GoldenAxeGame goldenAxeGame) {
        this.goldenAxeGame = goldenAxeGame;
    }

    @Override
    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public void start() {
        updateAspectRatioDimension();
        createBufferStrategy(2);
        bs = getBufferStrategy();
        //Audio.initialize();
        offscreen = new Offscreen(CANVAS_WIDTH, CANVAS_HEIGHT);
        goldenAxeGame.start();
        running = true;
        gameLoopThread = new Thread(new MainLoop());
        gameLoopThread.start();
        addKeyListener(new Input());
        addComponentListener(new ResizeListener());
    }
    
    private class ResizeListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            updateAspectRatioDimension();
        }
        
    }

    public void updateAspectRatioDimension() {
        int left = 0;
        int width = getWidth();
        int height = (int) (width / ASPECT_RATIO);
        int top = (int) ((getHeight() - height) / 2);
        if (top < 0) {
            top = 0;
            height = getHeight();
            width = (int) (height * ASPECT_RATIO);
            left = (int) ((getWidth() - width) / 2);
        }
        sizeWithAspectRatio.setBounds(left, top, width, height);
    }
    
    private class MainLoop implements Runnable {

        @Override
        public void run() {
            long currentTime = System.nanoTime();
            long lastTime = currentTime;
            long delta;
            long unprocessedTime = 0;
            while (running) {
                currentTime = System.nanoTime();
                delta = currentTime - lastTime;
                unprocessedTime += delta;
                lastTime = currentTime;
                goldenAxeGame.update(delta);
                while (unprocessedTime >= Settings.TIMER_PER_FRAME) {
                    unprocessedTime -= Settings.TIMER_PER_FRAME;
                    goldenAxeGame.fixedUpdate();
                }
                
                Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION
                        , RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
                goldenAxeGame.draw((Graphics2D) offscreen.getG2d());

                if (keepAspectRatio) {
                    g.clearRect(0, 0, getWidth(), getHeight());
                    g.drawImage(offscreen.getImage()
                        , sizeWithAspectRatio.x, sizeWithAspectRatio.y
                        , sizeWithAspectRatio.width, sizeWithAspectRatio.height
                        , null);
                }
                else {
                    g.drawImage(offscreen.getImage()
                            , 0, 0, getWidth(), getHeight(), null);
                }

                g.dispose();
                bs.show();
                
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException ex) {
//                }
            }
        }
        
    }
    
}
