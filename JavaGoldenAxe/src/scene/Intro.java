package scene;

import infra.AnimationPlayer;
import infra.Audio;
import static infra.Direction.*;
import infra.State;
import infra.GoldenAxeGame;
import infra.Input;
import infra.Offscreen;
import infra.Resource;
import infra.SceneManager;
import static infra.Settings.*;
import infra.TextRenderer;
import infra.Util;
import java.awt.Graphics2D;

/**
 * Intro class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Intro extends State<GoldenAxeGame, SceneManager> {

    private Offscreen offscreen;
    private AnimationPlayer animationPlayer;
    private long gameStartableBlinkStartTime;
    
    public Intro(SceneManager manager, GoldenAxeGame game) {
        super(manager, "intro", game);
        offscreen = new Offscreen(CANVAS_WIDTH, CANVAS_HEIGHT);
    }
    
    private void reset() {
        offscreen = null;
        animationPlayer = null;
    }
    
    @Override
    public void onEnter() {
        reset();
        gameStartableBlinkStartTime = System.nanoTime();
        offscreen = new Offscreen(CANVAS_WIDTH, CANVAS_HEIGHT);
        offscreen.getG2d().setColor(Util.getColor("0xe7e3e7"));
        offscreen.getG2d().fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        animationPlayer = Resource.getAnimationPlayer("intro", null);
        animationPlayer.setAnimation("intro_" + GoldenAxeGame.introIndex);
        Audio.stopMusic();
    }

    @Override
    public void onExit() {
        reset();
        System.gc();
    }
    
    @Override
    public void fixedUpdate() {
        animationPlayer.update();
        
        if (Input.isKeyJustPressed(KEY_START_1) && !manager.isTransiting()) {
            GoldenAxeGame.introIndex = 0;
            manager.switchTo("title");
        }
        else if (animationPlayer.getCurrentAnimation().isFinished()
                                            && !manager.isTransiting()) {

            if (GoldenAxeGame.introIndex < 3) {
                manager.switchTo("stage");
            }
            else {
                manager.switchTo("title");
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        animationPlayer.draw(offscreen.getG2d()
                , 0, 0, 0, 0, 0, RIGHT, false, 1.0, 1.0, 0.0);
        
        g.drawImage(offscreen.getImage(), 0, 0, null);
        
        long blinkTimeDif = System.nanoTime() - gameStartableBlinkStartTime;
        boolean blink = (int) (blinkTimeDif * 0.000000005) % 3 < 2;
        if (blink) {
            TextRenderer.draw(g, "PRESS SPACE TO START", 7, 14);
        }
    }

}
