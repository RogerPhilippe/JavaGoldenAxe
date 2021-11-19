package actor;

import infra.Actor;
import static infra.Actor.Control.*;
import infra.Animation;
import infra.AnimationPlayer;
import infra.Collider;
import infra.Direction;
import static infra.Direction.*;
import infra.GoldenAxeGame;
import infra.Input;
import infra.Resource;
import infra.Settings;
import static infra.Settings.*;
import infra.TextRenderer;
import infra.Util;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import scene.Stage;

/**
 * Player class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Player extends Actor {

    public static enum PlayerCharacter { 
        
        AX("ax_battler", 6),
        TYRIS("tyris_flare", 9), 
        GILIUS("gilius_thunderhead", 4);
        
        public final String id;
        public final int maxPotions;
        
        PlayerCharacter(String id, int maxPotions) {
            this.id = id;
            this.maxPotions = maxPotions;
        }
        
    }
    
    protected static BufferedImage[] hudLivesDigits = new BufferedImage[3];
    
    protected PlayerCharacter character;
    //protected int index; // player 1 or 2
    protected BufferedImage hudLogo;
    
    protected AnimationPlayer hudEnergyBar;
    protected int previouEnergy = -1;
    protected int hudEnergyBarX = -1;
    protected Direction hudEnergyBarDirection;
    protected long hudEnergyBarBlinkTime;
    
    protected AnimationPlayer hudMagic;
    protected int currentAnimationPotions = -1;
    protected int hudMagicX = -1;
    protected int hudMagicTextX = -1;
    protected long hudMagicBlinkTime;
    protected int score;
    protected boolean continueGame = true;
    protected long continueTimeout = -1;
    protected long gameOverTimeout = -1;
    protected boolean showingPrivateGameOver = false;
    protected BufferedImage arrow;
    
    public Enemy leftEnemy;
    public Enemy rightEnemy;
    
    static {
        BufferedImage hudLivesDigitsImage 
                        = Resource.getImage("hud_lives_digits");
        
        for (int n = 0; n < 3; n++) {
            hudLivesDigits[n] 
                        = hudLivesDigitsImage.getSubimage(n * 8, 0, 8, 15);
        }
    }
    
    public Player(Stage stage, PlayerCharacter character
                                    , Control control, int startPotionsCount) {
        
        super(stage, "player_" + control.index
                            , 0, 0, character.id, null, character);
        
        this.character = character;
        lives = 3;
        this.control = control;
        animationPlayer.setAnimation("logo");
        hudLogo = animationPlayer.getCurrentAnimation()
                        .getCurrentFrame().getSprite(0).getImage();
        
        for (int i = 0; i < startPotionsCount; i++) {
            potions.add(new Item(stage, Item.Type.MAGIC_POTION));
        }
        arrow = Resource.getImage("arrow_left");
    }
    
    @Override
    public void spawn(String[] data) {
        if (!isStarted()) {
            start();
            setStarted(true);
        }
        spawnInternal(data);
    }
    
    private void spawnInternal(String[] data) {
        // wx wz initial_state
        wx = Integer.parseInt(data[4]);
        wz = Integer.parseInt(data[5]);
        direction = RIGHT;
        ActorState actorState = (ActorState) stateManager.getState(data[6]);
        if (actorState != null) {
            actorState.spawn(data);
        }
        stateManager.switchTo(data[6]); 
    }

    @Override
    public void onPotionAdded(Item potion) {
        hudMagicBlinkTime = getTime() + 500;
    }
    
    @Override
    public void onEnergyRecovered() {
        hudEnergyBarBlinkTime = getTime() + 500;
    }

    public boolean isGameOver() {
        return credits == 0 && lives == 1 
                        && energy == 0 && !alive && isDestroyed();
    }
    
    public boolean needsToAskContinue() {
        return energy == 0 && lives == 1 && credits > 0 && !alive;
    }

    public boolean canGoNextStage() {
        return !needsToAskContinue() && !showingPrivateGameOver 
            && !stateManager.getCurrentState().getName().equals("using_magic");
    }

    public int getScore() {
        return score;
    }

    public double getStrength() {
        return 0.075 * (score / getUsedLives()); 
    }
    
    @Override
    public void start() {
        hudEnergyBar = Resource.getAnimationPlayer("hud_energy_bar", null);
        updateHudEnergyBar(0);
        startHudMagic("hud_magic_" + character.id, character.maxPotions);
    }
    
    public void startHudMagic(String animationRes, int maxPotions) {
        this.maxPotions = maxPotions;
        hudMagic = Resource.getAnimationPlayer(animationRes, null);
        updateHudMagic(0);
    }
    
    @Override
    public void fixedUpdate() {
        super.fixedUpdate(); 
        double frameSync = System.nanoTime() * 0.00000001;
        updateHudEnergyBar(frameSync);
        updateHudMagic(frameSync);
    }
    
    protected void updateHudEnergyBar(double frameSync) {
        if (hudEnergyBar != null) {
            if (energy != previouEnergy) {
                previouEnergy = energy;
                int barCount = (int) Math.ceil(energy / 16.0);
                hudEnergyBar.setAnimation("energy_" + barCount);
            }
            // ensure animation frames will be in sync for both players
            Animation currentAnimation = hudEnergyBar.getCurrentAnimation();
            if (currentAnimation != null) {
                hudEnergyBar.setCurrentAnimationFrameIndex(frameSync % 7);
            }
        }
    }
    
    private void updateHudMagic(double frameSync) {
        if (hudMagic != null) {
            if (currentAnimationPotions != potions.size()) {
                currentAnimationPotions = potions.size();
                hudMagic.setAnimation("potion_" + currentAnimationPotions);
            }
            // ensure animation frames will be in sync for both players
            Animation currentAnimation = hudMagic.getCurrentAnimation();
            if (currentAnimation != null && potions.size() > 0) {
                hudMagic.setCurrentAnimationFrameIndex(frameSync % 10);
            }
        }
    }
    
    public void updateAskContinue() {
        if (continueTimeout < 0) {
            continueTimeout = Util.getTime() + 9999;
        }
        else if (Util.getTime() >= continueTimeout) {
            continueGame = false;
            continueGameConfirmed();
        }
        if (control == PLAYER_1) {
            updateAskContinuePlayer1();
        }
        else if (control == PLAYER_2) {
            updateAskContinuePlayer2();
        }
    }

    private void updateAskContinuePlayer1() {
        if (Input.isKeyJustPressed(KEY_PLAYER_1_LEFT)) {
            continueGame = true;
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_1_RIGHT)) {
            continueGame = false;
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_1_ATTACK)) {
            continueGameConfirmed();
        }
    }

    private void updateAskContinuePlayer2() {
        if (Input.isKeyJustPressed(KEY_PLAYER_2_LEFT)) {
            continueGame = true;
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_2_RIGHT)) {
            continueGame = false;
        }
        else if (Input.isKeyJustPressed(KEY_PLAYER_2_ATTACK)) {
            continueGameConfirmed();
        }
    }

    private void continueGameConfirmed() {
        if (continueGame) {
            lives = LIVES + 1;
            energy = MAX_ENERGY;
            destroyed = false;
            tryNextLife();
            credits--;
            gameOverTimeout = -1;
        }
        else {
            credits = 0;
            lives = 1;
            energy = 0;
            alive = false;
            destroy();
        }
        continueGame = true;
        continueTimeout = -1;
    }

    public void drawHud(Graphics2D g) {
        showingPrivateGameOver = false;
        if (isGameOver()) {
            if (((control == PLAYER_1 && GoldenAxeGame.player2 != null 
                && !GoldenAxeGame.player2.isGameOver()) 
                    || (control == PLAYER_2 && GoldenAxeGame.player1 != null 
                        && !GoldenAxeGame.player1.isGameOver()))
                            && Util.getTime() < gameOverTimeout) {
                
                drawPrivateGameOver(g);
            }
        }
        else if (needsToAskContinue()) {
            drawAskContinue(g);
        }
        else {
            drawHudInternal(g);
        }
    }
    
    private void drawAskContinue(Graphics2D g) {
        int dx = control == PLAYER_1 ? 0 : 17; 
        String time = 
                "" + (1 + (int) ((continueTimeout - Util.getTime()) / 1000.0));
        
        boolean blink = (int) (System.nanoTime() * 0.000000005) % 3 > 0;
        TextRenderer.draw(g, credits + " CREDITS", 4 + dx, 16);
        if (blink) {
            TextRenderer.draw(g, "CONTINUE? " + time, 3 + dx, 18);
        }
        if (continueGame) {
            TextRenderer.draw(g, "YES  NO", 5 + dx, 19);
            g.drawImage(arrow, (8 + dx) * 8, 19 * 10 + 3, null);
        }
        else {
            TextRenderer.draw(g, "YES  NO ", 5 + dx, 19);
            g.drawImage(arrow, (12 + dx) * 8, 19 * 10 + 3, null);
        }
        
    }
    
    private void drawPrivateGameOver(Graphics2D g) {
        boolean blink = (int) (System.nanoTime() * 0.000000005) % 3 > 0;
        if (blink){
            int dx = control == PLAYER_1 ? 0 : 17; 
            TextRenderer.draw(g, "GAME OVER", 5 + dx, 19);
        }
        showingPrivateGameOver = true;
    }
    
    private void drawHudInternal(Graphics2D g) {
        // logo and lives
        if (control == PLAYER_1 && lives > 0) {
            g.drawImage(hudLogo, 116, 187, null);
            g.drawImage(hudLivesDigits[lives - 1], 108, 188, null);
        }
        else if (lives > 0) {
            g.drawImage(hudLogo, 144, 187, null);
            g.drawImage(hudLivesDigits[lives - 1], 161, 188, null);
        }
        // energy bar
        if (hudEnergyBar != null) {
            if (hudEnergyBarX < 0 && control == PLAYER_1) {
                hudEnergyBarX = Settings.CANVAS_WIDTH / 2 - 32;
                hudEnergyBarDirection = LEFT;
            }
            else if (hudEnergyBarX < 0 && control == PLAYER_2) {
                hudEnergyBarX = Settings.CANVAS_WIDTH / 2 + 31;
                hudEnergyBarDirection = RIGHT;
            }
            boolean blink = getTime() < hudEnergyBarBlinkTime;
            hudEnergyBar.draw(g, hudEnergyBarX, 191, 0, 0, 0
                        , hudEnergyBarDirection, blink, 1.0, 1.0, 0.0);
        }
        // magic
        if (hudMagic != null) {
            if (hudMagicX < 0 && control == PLAYER_1) {
                hudMagicTextX = 11 + 2;
                hudMagicX = Settings.CANVAS_WIDTH / 2 
                    - hudMagic.getCurrentAnimation().getCurrentFrame()
                        .getSprite(0).getImage().getWidth() - 10 + 16;
            }
            else if (hudMagicX < 0 && control == PLAYER_2) {
                hudMagicTextX = 18 + 2;
                hudMagicX = Settings.CANVAS_WIDTH / 2 + 6 + 16; 
            }
            boolean blink = getTime() < hudMagicBlinkTime;
            hudMagic.draw(g, hudMagicX, 8, 3, 0, 0, LEFT, blink, 1.0, 1.0, 0.0);
            if (potions.size() > 0) {
                TextRenderer.draw(g, "MAGIC", hudMagicTextX, 0);
            }
        }
    }
    
    public boolean isThrowingOrThrown() {
        String currentState = stateManager.getCurrentState().getName();
        return currentState.equals("grab_and_throw") 
                            || currentState.equals("thrown");
    }
    
    public boolean isActive() {
        String currentState = stateManager.getCurrentState().getName();
        return isAlive() && !isDestroyed() 
            && !currentState.equals("thrown") 
                && !currentState.equals("knock_down") 
                    && !currentState.equals("dying") 
                        && !currentState.equals("dead") 
                            && !currentState.equals("none");
    }
    
    public boolean isHittable() {
        String currentState = stateManager.getCurrentState().getName();
        return isAlive() && !isDestroyed() 
                &&!currentState.equals("knock_down") 
                    && !currentState.equals("stand_up") 
                        && !currentState.equals("resting")  
                            && !currentState.equals("dead") 
                                && !currentState.equals("dying")
                                    && !currentState.equals("none");
    }

    @Override
    public void onCollision(
        Collider thisCollider, Collider otherCollider, Actor collidedActor) {

        // TODO not implemented yet
        // Strength = ((0.5 + 0.5L) * E * A) * M. L
        // ref.: http://www.punchpedia.com/games/arc/goldenaxe/goldenaxe.php
        score += (int) ((0.5 + 0.5 * GoldenAxeGame.stage) 
                                                * thisCollider.getScore());
    }
    
    @Override
    public void onDeathHeight() {
        tryNextLife();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gameOverTimeout = Util.getTime() + 5000;
    }
    
}
