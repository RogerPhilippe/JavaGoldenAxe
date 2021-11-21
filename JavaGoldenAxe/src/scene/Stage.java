package scene;

import actor.Decoration;
import actor.Player;
import static actor.Player.PlayerCharacter.GILIUS;
import infra.Actor;
import static infra.Actor.Control.*;
import infra.Animation;
import infra.AnimationPlayer;
import infra.Animator;
import infra.Audio;
import infra.Camera;
import infra.Collider;
import infra.Go;
import infra.Offscreen;
import infra.State;
import infra.Terrain;
import infra.EnemiesManager;
import static infra.Collider.Type.*;
import infra.GoldenAxeGame;
import infra.Resource;
import infra.Background;
import infra.BackgroundTransition;
import infra.Dialog;
import static infra.Direction.*;
import infra.Input;
import infra.MagicBackgroundTransition;
import infra.SceneManager;
import static infra.Settings.*;
import infra.TextRenderer;
import static infra.TextRenderer.DEFAULT_FONT_COLOR;
import infra.Util;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stage class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Stage extends State<GoldenAxeGame, SceneManager> {

    public static final int MAX_BACKGROUND_LAYERS = 4;
    public static final int MAGIC_LAYER = 2;
    
    // 0~1=background 2=magic 3=top_layer
    private final BackgroundTransition[] backgrounds 
                            = new BackgroundTransition[MAX_BACKGROUND_LAYERS];
    
    private final Map<String, Map<String, Animator>> resAnimators = 
                                                            new HashMap<>();

    private final Offscreen shadowLayer;
    private final List<Actor> actors = new ArrayList<>();
    private final Map<String, Actor> actorsById = new HashMap<>();
    private final List<Actor> addActors = new ArrayList<>();
    private final List<Actor> removeActors = new ArrayList<>();
    private Animator currentAnimator;
    private double shadowShear = 0.0;
    private boolean gameOver;
    private long gameOverEndTime;
    private final AnimationPlayer gameOverAnimation;
    private boolean gameCleared;
    private int gameClearedState;
    private int gameClearedTextIndex;
    private long gameStartableBlinkStartTime;
    private long demoEndTime;
    private Actor boss;
    
    public Stage(SceneManager manager, GoldenAxeGame game) {
        super(manager, "stage", game);
        for (int layer = 0; layer < MAX_BACKGROUND_LAYERS; layer++) {
            switch (layer) {
                case MAGIC_LAYER
                        -> backgrounds[layer] = new MagicBackgroundTransition();
                        
                default -> backgrounds[layer] = new BackgroundTransition();
            }
        }
        shadowLayer = new Offscreen(CANVAS_WIDTH, CANVAS_HEIGHT);
        gameOverAnimation = Resource.getAnimationPlayer("game_over", null);
        gameOverAnimation.setAnimation("game_over");
    }

    public void addBackground(
            int layer, String transitionId, Background background) {
        
        backgrounds[layer].addBackground(transitionId, background);
    }

    public void addActor(Actor actor) {
        addActors.add(actor);
    }

    public void addActors(List<Actor> actors) {
        addActors.addAll(actors);
    }

    public Actor getActorById(String actorId) {
        return actorsById.get(actorId);
    }

    public List<Actor> getActors() {
        return actors;
    }

    public Map<String, Actor> getActorsById() {
        return actorsById;
    }

    public Animator getCurrentAnimator() {
        return currentAnimator;
    }

    public double getShadowShear() {
        return shadowShear;
    }

    public void setShadowShear(double shadowShear) {
        this.shadowShear = shadowShear;
    }
    
    public boolean isBossDefeated() {
        return boss != null && boss.getEnergy() == 0;
    }
    
    private void reset() {
        for (int layer = 0; layer < MAX_BACKGROUND_LAYERS; layer++) {
            backgrounds[layer].reset();
        }
        shadowLayer.clear();
        resAnimators.clear();
        actors.clear();
        actorsById.clear();
        addActors.clear();
        removeActors.clear();
        currentAnimator = null;
        shadowShear = 0.0;
        gameOver = false;
        gameCleared = false;
        gameClearedState = 0;
        gameClearedTextIndex = 0;
        gameStartableBlinkStartTime = System.nanoTime();
        demoEndTime = -1;
        boss = null;
    }
    
    @Override
    public void onEnter() {
        
        if (GoldenAxeGame.showingDemo) {
            GoldenAxeGame.player1 = new Player(this
                            , GoldenAxeGame.demoCharacter, PLAYER_1
                                , GoldenAxeGame.demoPotionsCount); 
            
            GoldenAxeGame.player1.setControlEnabled(false);
            
            GoldenAxeGame.player2 = null;
            GoldenAxeGame.stage = GoldenAxeGame.demoStage;
        }
        else {
            GoldenAxeGame.stage++;
            if (GoldenAxeGame.characterPlayer1 != null 
                    && GoldenAxeGame.player1 == null) {

                GoldenAxeGame.player1 = new Player(
                        this, GoldenAxeGame.characterPlayer1, PLAYER_1, 1);
            }
            if (GoldenAxeGame.characterPlayer2 != null 
                                && GoldenAxeGame.player2 == null) {

                GoldenAxeGame.player2 = new Player(
                        this, GoldenAxeGame.characterPlayer2, PLAYER_2, 1);
            }
            
        }
        
        //GoldenAxeGame.player1 = new Player(this, GILIUS, PLAYER_1, 2); // TODO TEST REMOVE LATER 
        //GoldenAxeGame.player2 = new Player(this, TYRIS, PLAYER_2, 3); // TODO TEST REMOVE LATER 
        
        clearAll();
        Resource.loadStage(GoldenAxeGame.stage, this);
        actors.forEach(actor -> actor.start());
        
        for (int i = 0; i < 3; i++) {
            fixedUpdate();
        }
    }
    
    private void clearAll() {
        Resource.clear();
        reset();
        Camera.reset(this);
        Terrain.reset();
        EnemiesManager.reset(this);
    }
    
    @Override
    public void onExit() {
        Audio.stopMusic();
        clearAll();
        System.gc();
    }
    
    @Override
    public void fixedUpdate() {
        // in demo mode
        if (GoldenAxeGame.showingDemo && Input.isKeyJustPressed(KEY_START_1)) {
            manager.switchTo("title");
            return;
        }

        if (currentAnimator != null) {
            currentAnimator.fixedUpdate(actorsById);
            if (currentAnimator.isFinished()) {
                currentAnimator = null;
            }
        }
        Dialog.update();
        updateShadowShearTransition();
        Go.update();
        EnemiesManager.update();
        for (int layer = 0; layer < MAX_BACKGROUND_LAYERS; layer++) {
            backgrounds[layer].update();
        }
        if (!addActors.isEmpty()) {
            actors.addAll(addActors);
            addActors.forEach(actor -> {
                if (actor.isBoss()) {
                    boss = actor;
                }
                if (!actor.isStarted()) {
                    actor.start();
                    actor.setStarted(true);
                }
                actorsById.put(actor.getId(), actor);
            });
            addActors.clear();
        }
        Collections.sort(actors);
        actors.forEach(actor -> {
            actor.fixedUpdate();
            if (actor.isDestroyed()) {
                removeActors.add(actor);
            }
        });
        if (!removeActors.isEmpty()) {
            actors.removeAll(removeActors);
            removeActors.forEach(actor -> actorsById.remove(actor.getId()));
            removeActors.clear();
        }
        Camera.update();
        checkCollisions();
        if (GoldenAxeGame.showingDemo) {
            checkDemoFinished();
        }
        else {
            checkGameOver();
            checkGameCleared();
        }
    }
    
    private void checkDemoFinished() {
        if (demoEndTime >= 0 && Util.getTime() >= demoEndTime) {
            GoldenAxeGame.setNextDemoCharacter();
            manager.switchTo("intro");
        }
        else if (demoEndTime < 0 && GoldenAxeGame.player1.getStateManager()
                            .getCurrentState().getName().equals("walking")) {
            demoEndTime = Util.getTime() + 2000;
        }
    }
    
    private void checkGameCleared() {
        if (!gameOver && !gameCleared 
                && (GoldenAxeGame.stage == 9 || GoldenAxeGame.stage == 10) 
                    && Camera.isStageCleared()) {
            
            Audio.stopMusic();
            gameCleared = true;
        }
        
        if (gameCleared && canScrollStageHorizontally()) {
            switch (gameClearedState) {
                // move players until up corners
                case 0:
                    if (GoldenAxeGame.player1 != null 
                            && GoldenAxeGame.player1.isActive()) {
                        
                        GoldenAxeGame.player1.setControlEnabled(false);
                        GoldenAxeGame.player1.walkTo(
                                6120, 272, "walking", RIGHT);
                    }
                    if (GoldenAxeGame.player2 != null 
                            && GoldenAxeGame.player2.isActive()) {
                        
                        GoldenAxeGame.player2.setControlEnabled(false);
                        GoldenAxeGame.player2.walkTo(
                                6350, 272, "walking", LEFT);
                    }
                    gameClearedState = 1;
                    break;

                // waits for players to arrive in the correct positions
                case 1:
                    if (GoldenAxeGame.player1 != null 
                        && GoldenAxeGame.player1.isActive() 
                            && GoldenAxeGame.player1.getStateManager()
                            .getCurrentState().getName().equals("walk_to")) { 
                        
                        return;
                    }
                    if (GoldenAxeGame.player2 != null 
                        && GoldenAxeGame.player2.isActive() 
                            && GoldenAxeGame.player2.getStateManager()
                            .getCurrentState().getName().equals("walk_to")) { 
                        
                        return;
                    }
                    gameClearedState = 2;
                    break;

                // shows the animation of the prince and princess being freed
                case 2:
                    Audio.playNextMusic("conclusion", 0.1, 0);
                    playAnimator("highness", "rescued");
                    gameClearedState = 3;
                    break;
                    
                // wait prince and princess being freed animation finish
                case 3:
                    if (!isAnimatorFinished("highness", "rescued")) {
                        return;
                    }
                    gameClearedState = 4;
                    break;
                    
                // show the final dialogs
                case 4:
                    String text = Resource.getText(
                            "end_dialog_text_" + gameClearedTextIndex);
                    
                    Dialog.show(
                            7, 6, 19, 4, text, true, DEFAULT_FONT_COLOR, null);
                    
                    gameClearedState = 5;
                    break;

                // check active players keypress to skip the dialog
                case 5:
                    boolean keyJustPressed = Input.isKeyJustPressed(KEY_START_1) 
                                        || Input.isKeyJustPressed(KEY_START_2);

                    keyJustPressed = keyJustPressed 
                        || (GoldenAxeGame.player1 != null 
                            && GoldenAxeGame.player1.isActive() 
                                && Input.isKeyJustPressed(KEY_PLAYER_1_ATTACK));
                    
                    keyJustPressed = keyJustPressed 
                        || (GoldenAxeGame.player2 != null 
                            && GoldenAxeGame.player2.isActive() 
                                && Input.isKeyJustPressed(KEY_PLAYER_2_ATTACK));

                    if (!Dialog.isFinished() && keyJustPressed) {
                        Dialog.showAll();
                    }
                    else if (Dialog.isFinished() 
                            && gameClearedTextIndex < 4 && keyJustPressed) {

                        gameClearedTextIndex++;
                        gameClearedState = 4;
                    }
                    else if (Dialog.isFinished() && keyJustPressed) {
                        Dialog.hide();
                        goNext();
                        gameClearedState = 6;
                    }
                    break;
            }                    
        }
    }
    
    private void checkGameOver() {
        if (GoldenAxeGame.player1 != null 
                && GoldenAxeGame.player1.needsToAskContinue()) {
            
            GoldenAxeGame.player1.updateAskContinue();
        }

        if (GoldenAxeGame.player2 != null 
                && GoldenAxeGame.player2.needsToAskContinue()) {
            
            GoldenAxeGame.player2.updateAskContinue();
        }
 
        if (gameOver && Util.getTime() >= gameOverEndTime) {
            manager.switchTo("ranking");
        }
        else if (gameOver) {
            gameOverAnimation.update();
        }
        else if (!gameOver && (GoldenAxeGame.player1 == null 
                || GoldenAxeGame.player1.isGameOver())
                    && (GoldenAxeGame.player2 == null 
                        || GoldenAxeGame.player2.isGameOver())) {
            
            gameOver = true;
            gameOverEndTime = Util.getTime() + 8000;
            Audio.playNextMusic("game_over", 0.2, 250);
        }
    }

    private final Collider colliderA1Tmp = new Collider(0, 0, 0, 0, 0, 0);
    private final Collider colliderA2Tmp = new Collider(0, 0, 0, 0, 0, 0);
    
    private void checkCollisions() {
        for (Actor a1 : actors) {
            for (Actor a2 : actors) {
                if (a1 == a2 
                        || !a1.isCollisionCheckEnabled() 
                        || !a2.isCollisionCheckEnabled()
                        // enemy can't hit other enemies
                        || a1.getControl() == IA && a2.getControl() == IA) {
                    
                    continue;
                }

                Animation animationA1 
                        = a1.getAnimationPlayer().getCurrentAnimation();

                Animation animationA2 
                        = a2.getAnimationPlayer().getCurrentAnimation();

                if (animationA1 == null || animationA2 == null 
                    || !animationA1.isCollisionCheckEnabled() 
                    || !animationA2.isCollisionCheckEnabled() 
                        || !a1.isAlive() || !a2.isAlive()
                        || a1.isDestroyed() || a2.isDestroyed()
                            || Math.abs(a1.getWz() - a2.getWz()) 
                                > VALID_COLLISION_WZ_DIF) {
                    
                    continue;
                }
                
                outer:
                for (Collider colliderA1 : a1.getCurrentFrameColliders()) {
                    for (Collider colliderA2 : a2.getCurrentFrameColliders()) {
                        
                        boolean condition1 = colliderA1.getType() == ATTACK 
                                && colliderA2.getType() == BODY
                                && !a2.isInvincible();
                        
                        boolean condition2 = colliderA1.getType() == TOUCH 
                                && colliderA2.getType() == BODY;

                        boolean condition3 = colliderA1.getType() == FIRE 
                                && colliderA2.getType() == BODY
                                    && a2.isFireControlEnabled();
                        
                        if ((condition1 || condition2 || condition3)
                                && !animationA1.isActorReacted(a2)) {
                            
                            a1.convertColliderToCameraSpace(
                                    colliderA1, colliderA1Tmp
                                        , Camera.getX(), Camera.getY());
                            
                            a2.convertColliderToCameraSpace(
                                    colliderA2, colliderA2Tmp
                                        , Camera.getX(), Camera.getY());
                            
                            if (colliderA1Tmp.intersects(colliderA2Tmp)) {
                                animationA1.addReactedActor(a2);
                                a1.setLastHitDistance(
                                        Math.abs(a1.getWx() - a2.getWx()));
                                
                                a1.onCollision(colliderA1, colliderA2, a2);
                                a2.react(
                                    colliderA1.getReaction(), colliderA1, a1);
                                
                                break outer;
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void draw(Graphics2D g) {
        final int cameraX = (int) Camera.getX();
        final int cameraY = (int) Camera.getY();
        shadowLayer.clear();
        for (int layer = 0; layer < MAX_BACKGROUND_LAYERS - 2; layer++) {
            backgrounds[layer].draw(g, cameraX, cameraY);
        }
        actors.forEach(actor -> {
            if (actor instanceof Decoration decoration 
                            && decoration.isDrawingBeforeShadow()) {
                
                actor.draw(g, cameraX, cameraY);
            }
        });
        backgrounds[MAGIC_LAYER].draw(g, cameraX, cameraY);
        Collections.sort(actors);
        actors.forEach(actor ->  
            actor.drawShadow(shadowLayer, cameraX, cameraY, shadowShear));
        
        g.drawImage(shadowLayer.getImage(), 0, 0, null);
        Terrain.drawDebugHeightImage(g, cameraX, cameraY);
        actors.forEach(actor -> {
            if (!(actor instanceof Decoration decoration) ||
                            !decoration.isDrawingBeforeShadow()) {

                actor.draw(g, cameraX, cameraY);
            }
        });
        backgrounds[MAX_BACKGROUND_LAYERS - 1].draw(g, cameraX, cameraY);
        if (GoldenAxeGame.showingDemo) {
            long blinkTimeDif = System.nanoTime() - gameStartableBlinkStartTime;
            boolean blink = (int) (blinkTimeDif * 0.000000005) % 3 < 2;
            if (blink) {
                TextRenderer.draw(g, "PRESS SPACE TO START", 7, 16);
            }
        }
        else {
            Go.draw(g);
            Dialog.draw(g);
            drawHud(g);
        }
        if (gameOver) {
            gameOverAnimation.draw(
                    g, 70, 0, 85, 0, 0, RIGHT, false, 1.0, 1.0, 0.0);
        } 
        if (SHOW_DEBUG_CAMERA_INFO) {
            String cameraInfo = String.format("camera x=%d y=%d min=%d max=%d"
                , (int) Camera.getX(), (int) Camera.getY()
                , (int) Camera.getMinY((int) (Camera.getX() + CANVAS_WIDTH))
                , (int) Camera.getMaxY((int) (Camera.getX() + CANVAS_WIDTH)));
            
            g.setColor(Color.WHITE);
            g.drawString(cameraInfo, 8, 40);
        }
        if (SHOW_DEBUG_PLAYER_1_INFO && GoldenAxeGame.player1 != null) {
            String player1Info = String.format("p1 x=%d y=%d z=%d sc=%d"
                    , (int) GoldenAxeGame.player1.getWx()
                    , (int) GoldenAxeGame.player1.getWy()
                    , (int) GoldenAxeGame.player1.getWz()
                    , (int) GoldenAxeGame.player1.getScore());
            g.setColor(Color.WHITE);
            g.drawString(player1Info, 8, 48);
        }
        if (SHOW_DEBUG_PLAYER_2_INFO && GoldenAxeGame.player2 != null) {
            String player2Info = String.format("p2 x=%d y=%d z=%d sc=%d"
                    , (int) GoldenAxeGame.player2.getWx()
                    , (int) GoldenAxeGame.player2.getWy()
                    , (int) GoldenAxeGame.player2.getWz()
                    , (int) GoldenAxeGame.player2.getScore());
            g.setColor(Color.WHITE);
            g.drawString(player2Info, 8, 56);
        }
        if (SHOW_DEBUG_ENEMIES_COUNT) {
            g.drawString("count boss=" + Camera.getBossEnemiesCount() 
                    + " not_boss=" + Camera.getNotBossEnemiesCount() 
                        + " active=" + Camera.getActiveEnemiesCount(), 8, 64);
        }
    }

    private void drawHud(Graphics2D g) {
        TextRenderer.draw(g, "STAGE " + GoldenAxeGame.stage, 1, 1);
        if (GoldenAxeGame.player1 != null) {
            GoldenAxeGame.player1.drawHud(g);
        }
        if (GoldenAxeGame.player2 != null) {
            GoldenAxeGame.player2.drawHud(g);
        }
    }

    public void setBackgroundEnabled(int layer, boolean backgroundEnabled) {
        backgrounds[layer].setEnabled(backgroundEnabled);
    }

    public void startBackgroundTransition(int layer
            , String transitionId, double transitionStep, long delayTime) {
        
        backgrounds[layer].startTransition(
                transitionId, transitionStep, delayTime);
    }

    public void startBackgroundVelocityTransition(int layer
                , String backgroundId, double targetVx, double targetVy
                    , double transitionSpeed) {
        
        backgrounds[layer].startVelocityTransition(
                backgroundId, targetVx, targetVy, transitionSpeed);
    }

    public BackgroundTransition getBackground(int layer) {
        return backgrounds[layer];
    }

    
    // --- shadow shear transition ---
    
    private double shadowShearCurrent;
    private double shadowShearTarget;
    private double shadowShearTransitionProgress;
    private double shadowShearTransitionSpeed;
    
    public void startShadowShearTransition(
            double shadowShearTarget, double transitionSpeed) {
        
        this.shadowShearCurrent = shadowShear;
        this.shadowShearTarget = shadowShearTarget;
        this.shadowShearTransitionSpeed = transitionSpeed;
        this.shadowShearTransitionProgress = 0.0;
    }
    
    private void updateShadowShearTransition() {
        if (shadowShearTransitionProgress < 1.0) {
            shadowShearTransitionProgress += shadowShearTransitionSpeed;
            if (shadowShearTransitionProgress >= 1.0) {
                shadowShearTransitionProgress = 1.0;
                shadowShear = shadowShearTarget;
            }
            else {
                double dif = shadowShearTarget - shadowShearCurrent;
                shadowShear = shadowShearCurrent 
                        + shadowShearTransitionProgress * dif;
            }
        }
    }

    // --- game flow ---

    public boolean canScrollStageHorizontally() {
        if ((GoldenAxeGame.player1 == null 
                || !GoldenAxeGame.player1.needsToAskContinue()
                    && (GoldenAxeGame.player2 == null 
                        || !GoldenAxeGame.player2.needsToAskContinue()))) {
        
            boolean player1Ok = GoldenAxeGame.player1 == null 
                    || GoldenAxeGame.player1.isGameOver() 
                        || (GoldenAxeGame.player1.isActive()
                            && !GoldenAxeGame.player1.isJumping());
            
            boolean player2Ok = GoldenAxeGame.player2 == null 
                    || GoldenAxeGame.player2.isGameOver() 
                        || (GoldenAxeGame.player2.isActive()
                            && !GoldenAxeGame.player1.isJumping());
            
            return player1Ok && player2Ok && !gameOver;
        }
        return false;
    }
    
    public boolean canGoNextStage() {
        if ((GoldenAxeGame.player1 == null 
                || GoldenAxeGame.player1.canGoNextStage())
                    && (GoldenAxeGame.player2 == null 
                        || GoldenAxeGame.player2.canGoNextStage())) {
        
            boolean player1Ok = GoldenAxeGame.player1 == null 
                    || GoldenAxeGame.player1.isGameOver() 
                        || (GoldenAxeGame.player1.isActive()
                            && !GoldenAxeGame.player1.isJumping());
            
            boolean player2Ok = GoldenAxeGame.player2 == null 
                    || GoldenAxeGame.player2.isGameOver() 
                        || (GoldenAxeGame.player2.isActive()
                            && !GoldenAxeGame.player2.isJumping());
            
            return player1Ok && player2Ok && !gameOver;
        }
        return false;
    }
    
    public void goNext() {
        if (GoldenAxeGame.stage % 2 == 0 
                || GoldenAxeGame.stage == 9 || GoldenAxeGame.stage == 10) {
            
            Audio.stopMusic();
            manager.switchTo("old_map");
        }
        else {
            Audio.stopMusic();
            manager.switchTo("stage");
        }
    }
    
    public void loadAnimator(String res) {
        if (!resAnimators.containsKey(res)) {
            Map<String, Animator> animators = Resource.getAnimators(res);
            resAnimators.put(res, animators);
        }
    }
    
    public void playAnimator(String res, String animatorId) {
        Map<String, Animator> animators = resAnimators.get(res);
        currentAnimator = animators.get(animatorId);
        currentAnimator.play();
    }

    private boolean isAnimatorFinished(String res, String animatorId) {
        Map<String, Animator> animators = resAnimators.get(res);
        currentAnimator = animators.get(animatorId);
        return currentAnimator.isFinished();
    }

}
