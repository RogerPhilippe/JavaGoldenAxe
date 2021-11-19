package scene;

import infra.Animator;
import infra.Animator.AnimatorEvent;
import infra.AnimatorEventListener;
import infra.Audio;
import infra.Background;
import infra.BackgroundTransition;
import infra.Dialog;
import infra.GoldenAxeGame;
import infra.Input;
import infra.Offscreen;
import infra.Resource;
import infra.SceneManager;
import static infra.Settings.*;
import infra.State;
import infra.Terrain;
import infra.TextRenderer;
import infra.Util;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Title class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com);
 */
public class Title extends State<GoldenAxeGame, SceneManager> 
                                        implements AnimatorEventListener {
    
    private Offscreen collisionMask;
    private Offscreen shadowLayer;
    private Animator animator;
    private Animator animatorCredits;
    private Animator animatorGameStartOptions;
    private BackgroundTransition backgrounds;
    
    private boolean gameStartable;
    private long gameStartableBlinkStartTime;
    
    private boolean gameStartOptionVisible;
    private int selectedGameStartOptionIndex;
    
    private final BufferedImage arrow;
    
    public Title(SceneManager manager, GoldenAxeGame game) {
        super(manager, "title", game);
        arrow = Resource.getImage("arrow_right");
    }
    
    private void reset() {
        collisionMask = null;
        shadowLayer = null;
        animator = null;
        backgrounds = null;
        gameStartable = false;
        gameStartableBlinkStartTime = 0;
        gameStartOptionVisible = false;
        selectedGameStartOptionIndex = 0;
        Terrain.reset();
        Dialog.hide();
    }
    
    @Override
    public void onEnter() {
        GoldenAxeGame.reset();
        reset();
        collisionMask = new Offscreen(CANVAS_WIDTH, CANVAS_HEIGHT);
        collisionMask.getG2d().setColor(Util.getColor("0x998010"));
        collisionMask.getG2d().fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        shadowLayer = new Offscreen(CANVAS_WIDTH, CANVAS_HEIGHT);
        Terrain.setCollisionMask(collisionMask.getImage());
        Map<String, Animator> animators = Resource.getAnimators("title");
        animator = animators.get("title");
        animatorCredits = animators.get("title_credits");
        animatorGameStartOptions = animators.get("title_game_start_options");
        animator.setListener(this);
        animatorGameStartOptions.setListener(this);
        Background backgroundNormal = new Background("title_background"
                                        , 0.0, -4.0, 0.0, 0.0, 1.0, 1.0, true);

        Background backgroundDarker = new Background("title_background_darker"
                                        , 0.0, -4.0, 0.0, 0.0, 1.0, 1.0, true);

        backgrounds = new BackgroundTransition();
        backgrounds.addBackground("normal", backgroundNormal);
        backgrounds.addBackground("darker", backgroundDarker);
        animator.play();
        animator.fixedUpdate();
        animatorCredits.play();
        animatorCredits.fixedUpdate();
        
        Audio.playMusic("title");
    }

    @Override
    public void onExit() {
        reset();
        System.gc();
    }
    
    @Override
    public void update(long delta) {
        double lerp = delta / (double) TIMER_PER_FRAME;
        animatorCredits.update(lerp);
        animator.update(lerp);
        animatorGameStartOptions.update(animator.getActorsById(), lerp);
        if (gameStartOptionVisible) {
            updateGameStartOption();
        }
    }   
    
    private void updateGameStartOption() {
        if (Input.isKeyJustPressed(KEY_PLAYER_1_DOWN)) {
            selectedGameStartOptionIndex++;
            if (selectedGameStartOptionIndex > 2) {
                selectedGameStartOptionIndex = 0;
            }
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_1_UP)) {
            selectedGameStartOptionIndex--;
            if (selectedGameStartOptionIndex < 0) {
                selectedGameStartOptionIndex = 2;
            }
        }
        else if (Input.isKeyJustPressed(KEY_START_1) 
                        || Input.isKeyJustPressed(KEY_START_2)) {
            
            switch (selectedGameStartOptionIndex) {
                case 0 -> goToSelectPlayerScene(1);
                case 1 -> goToSelectPlayerScene(2);
                case 2 -> System.exit(0);
            }
        }
    }

    private void goToSelectPlayerScene(int numberOfPlayers) {
        GoldenAxeGame.numberOfPlayers = numberOfPlayers;
        Audio.playNextMusic("empty", 0.1, 0);
        manager.switchTo("select_player");
    }
    
    @Override
    public void fixedUpdate() {
        backgrounds.update();
        if (gameStartable && !animatorGameStartOptions.isPlaying()
                && Input.isKeyJustPressed(KEY_START_1) 
                    && !manager.isTransiting()) {

            animator.pause();
            animatorGameStartOptions.play();
            animatorGameStartOptions.fixedUpdate(animator.getActorsById());
        }
    }

    @Override
    public void draw(Graphics2D g) {
        backgrounds.draw(g, 0.0, 0.0);
        shadowLayer.clear();
        
        if (animator.isPlaying()) {
            animator.drawShadow(shadowLayer, 0, 0, 1.0);
            animator.draw(g, shadowLayer, 0, 0);
        }
        
        if (animatorGameStartOptions.isPlaying()) {
            animatorGameStartOptions.drawShadow(
                    animator.getActors(), shadowLayer, 0, 0, 1.0);
            
            animatorGameStartOptions.draw(
                    animator.getActors(), g, shadowLayer, 0, 0);
        }
        else if (gameStartable) {
            long blinkTimeDif = System.nanoTime() - gameStartableBlinkStartTime;
            boolean blink = (int) (blinkTimeDif * 0.000000005) % 3 < 2;
            if (blink) {
                TextRenderer.draw(g, "PRESS SPACE TO START", 7, 16);
            }
        }
        if (gameStartOptionVisible) {
            drawGameStartOptionsMenu(g);
        }
        animatorCredits.draw(g, shadowLayer, 0.0, 0.0);
    }
    
    public void drawGameStartOptionsMenu(Graphics2D g) {
        TextRenderer.draw(g, "1P START", 13, 9);
        TextRenderer.draw(g, "2P START", 13, 11);
        TextRenderer.draw(g, "QUIT", 13, 13);
        //TextRenderer.draw(g, ">", 12, 9 + 2 * selectedGameStartOptionIndex);
        g.drawImage(arrow, 96, 93 + 20 * selectedGameStartOptionIndex, null);
    }

    @Override
    public void onAnimatorEventTriggered(AnimatorEvent event) {
        switch (event.eventId) {
            case "invert_sky_vy_direction" -> {
                backgrounds.startVelocityTransition("normal", 0.0, 0.5, 0.1);
                backgrounds.startVelocityTransition("darker", 0.0, 0.5, 0.1);
            }
            case "make_sky_darker" -> {
                backgrounds.startTransition("darker", 0.03, 0);
            }
            case "game_startable" -> {
                gameStartable = true;
                gameStartableBlinkStartTime = System.nanoTime();
            }
            case "game_start_option_visible" -> {
                gameStartOptionVisible = true;
            }
            case "show_intro" -> {
                Audio.stopMusic();
                GoldenAxeGame.setNextDemoCharacter();
                manager.switchTo("intro");
            }
        }
    }
    
}
