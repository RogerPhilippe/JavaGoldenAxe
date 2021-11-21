package infra;

import actor.Dust;
import actor.Enemy;
import actor.Item;
import actor.Player.PlayerCharacter;
import actor.Projectile;
import actor.Thief;
import actor.magic.FireA;
import actor.magic.FireB;
import actor.magic.FireC;
import actor.magic.FireD;
import actor.magic.FireE;
import actor.magic.FireF;
import actor.magic.LightningA;
import actor.magic.LightningB;
import actor.magic.LightningC;
import actor.magic.VolcanoABC;
import static actor.magic.VolcanoABC.Type.*;
import actor.magic.VolcanoD;
import static infra.Actor.Control.*;
import infra.Animation.Frame;
import infra.Animation.FramePoint;
import infra.Animator.AnimatorFrame;
import infra.Collider.Reaction;
import static infra.Collider.Reaction.*;
import static infra.Direction.*;
import static infra.Settings.*;
import static infra.TextRenderer.*;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import scene.Stage;
import static scene.Stage.*;

/**
 * Actor class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Actor extends Entity implements Comparable<Actor> {

    public static enum Control { NONE(0), PLAYER_1(1), PLAYER_2(2), IA(3); 

        public final int index;

        private Control(int index) {
            this.index = index;
        }

    }
    
    protected String subtype;
    
    // life
    protected int credits = CREDITS;
    protected int lives = LIVES;
    protected int energy = MAX_ENERGY;

    // sprite transformation
    protected double spriteScaleX = 1.0;
    protected double spriteScaleY = 1.0;
    protected double spriteAngle = 0.0;
    
    // world space coordinates
    protected double wx = 0.0;
    protected double wy = 0.0;
    protected double wz = 0.0;
    protected double lastValidWx = 0.0;
    protected double lastValidWz = 0.0;
    
    // velocity
    protected double vx = 0.0;
    protected double vy = 0.0;
    protected double vz = 0.0;
    
    protected double walkSpeed = 1.0;
    protected double runSpeed = 2.5;
    protected double jumpWalkSpeed = 5.0;
    protected double jumpRunSpeed = 6.5;
    protected int collisionMargin = 8;
    
    // controls
    protected Control control = Control.NONE;
    protected boolean controlEnabled = true;
    protected boolean walkLeftControlEnabled;
    protected boolean walkRightControlEnabled;
    protected boolean runLeftControlEnabled;
    protected boolean runRightControlEnabled;
    protected boolean upControlEnabled;
    protected boolean downControlEnabled;
    protected boolean fireControlEnabled;
    protected boolean fire2ControlEnabled;
    protected boolean jumpControlEnabled;
    protected boolean magicControlEnabled;
    protected boolean projectileControlEnabled;

    protected final StateManager<Actor, StateManager> stateManager;
    protected final AnimationPlayer animationPlayer;
    protected String lockingCameraId; // null = not locking camera
    protected int horizontalCameraLimitMarginX = 0;
    protected boolean alive = true;
    protected boolean checkDeathHeight = true;
    protected boolean destroyIfDeathHeight = false;
    protected boolean destroyIfNotAlive = false;
    protected boolean collisionCheckEnabled = true;
    protected boolean running;
    protected boolean onFloor;
    protected double floorHeight;
    protected boolean jumping;
    protected double jumpHeight;
    protected boolean jumpHeightInterpolationEnabled = true;
    protected Direction direction = Direction.RIGHT;
    protected boolean setDirectionOnUpdateVelocity = true;
    protected boolean blinking = false;
    protected double lastHitDistance = 16;
    protected Actor throwingActor;
    protected boolean mountable;
    protected Actor mountedActor;
    protected Actor mountingActor;
    protected long mountCount;
    protected long maxMountCount = 3 + Util.random(3);
    protected long mountTimeout = -1;
    protected boolean invincible;
    protected long invincibleTimeout;
    protected boolean paused;
    protected int maxPotions;
    protected List<Item> potions = new ArrayList<>();
    protected boolean boss;
    protected int deathCount;
    
    public Actor(Stage stage, String id, int wx, int wz, String characterId
                            , String subtype, PlayerCharacter playerCharacter) {
        
        super(stage, id);
        this.subtype = subtype;
        stateManager = new StateManager<>();
        animationPlayer = characterId != null 
                ? Resource.getAnimationPlayer(characterId, subtype) : null;
        
        this.wx = wx;
        this.wz = wz;
        if (animationPlayer != null) {
            createAllAvailableStates(playerCharacter);
            stateManager.switchTo("none");
        }
    }
    
    private void createAllAvailableStates(PlayerCharacter playerCharacter) {
        stateManager.addState(new None());
        if (animationPlayer.isAnimationAvailable("appearance")) {
            stateManager.addState(new Appearance());
        }
        if (animationPlayer.isAnimationAvailable("stand_by")) {
            stateManager.addState(new StandBy());
        }
        if (animationPlayer.isAnimationAvailable("walking_up")
                && animationPlayer.isAnimationAvailable("walking_down")) {
            
            stateManager.addState(new Walking());
        }
        if (animationPlayer.isAnimationAvailable("running")) {
            stateManager.addState(new Running());
        }
        if (animationPlayer.isAnimationAvailable("jumping")) {
            stateManager.addState(new Jumping());
        }
        for (int i = 1; i <= 7; i++) {
            if (animationPlayer.isAnimationAvailable("attack_" + i)) {
                stateManager.addState(new Attack(i));
            }
        }
        if (animationPlayer.isAnimationAvailable("attack_special")) {
            stateManager.addState(new AttackSpecial());
        }
        if (animationPlayer.isAnimationAvailable("attack_short")) {
            stateManager.addState(new Attack("short"));
        }
        if (animationPlayer.isAnimationAvailable("attack_long")) {
            stateManager.addState(new Attack("long"));
        }
        if (animationPlayer.isAnimationAvailable("attack_projectile")) {
            stateManager.addState(new Attack("projectile"));
        }
        for (int i = 1; i <= 2; i++) {
            if (animationPlayer.isAnimationAvailable("stunned_" + i)) {
                stateManager.addState(new Actor.Stunned(i));
            }
        }        
        if (animationPlayer.isAnimationAvailable("knock_down")) {
            stateManager.addState(new KnockDown());
        }
        if (animationPlayer.isAnimationAvailable("stand_up")) {
            stateManager.addState(new StandUp());
        }
        if (animationPlayer.isAnimationAvailable("grab_and_throw")) {
            stateManager.addState(new GrabAndThrow());
        }
        if (animationPlayer.isAnimationAvailable("thrown")) {
            stateManager.addState(new Thrown());
        }
        if (animationPlayer.isAnimationAvailable("resting")) {
            stateManager.addState(new Resting());
        }
        if (animationPlayer.isAnimationAvailable("magic") 
                                            && playerCharacter != null) {
            
            stateManager.addState(new UsingMagic(playerCharacter));
        }        
        if (animationPlayer.isAnimationAvailable("runaway")) {
            stateManager.addState(new Runaway());
        }        
        if (animationPlayer.isAnimationAvailable("dying")) {
            stateManager.addState(new Dying());
        }
        if (animationPlayer.isAnimationAvailable("dead")) {
            stateManager.addState(new Dead());
        }
        if (animationPlayer.isAnimationAvailable("mounting")) {
            stateManager.addState(new Mounting());
            stateManager.addState(new Mounted());
        }        
        if (animationPlayer.isAnimationAvailable("golden_axe")) {
            stateManager.addState(new GoldenAxe());
        }
        stateManager.addState(new WaitUntilActorsDeath());
        stateManager.addState(new WalkTo());
        stateManager.addState(new ShowStartDialogText());
        stateManager.addState(new PlayingAnimator());
        stateManager.addState(new Demo());
    }
    
    public void reset() {
        vx = 0;
        vy = 0;
        vz = 0;
        wy = Terrain.MAX_HEIGHT;
        updateMovement();
        controlEnabled = !GoldenAxeGame.showingDemo;
    }
    
    public void spawn(String[] data) {
    }

    public int getEnergy() {
        return energy;
    }

    public void recoverEnergy() {
        if (energy < MAX_ENERGY) {
            energy += 16;
            if (energy > MAX_ENERGY) {
                energy = MAX_ENERGY;
            }
            onEnergyRecovered();
        }
    }

    public void onEnergyRecovered() {
    }

    public void hit(int damage) {
        energy -= damage;
        if (energy < 0) {
            energy = 0;
        }
    }

    public boolean isMountable() {
        return mountable;
    }

    public void mount(Actor mountingActor) {
        this.mountingActor = mountingActor;
        control = mountingActor.getControl();
        walkSpeed = mountingActor.getWalkSpeed();
        mountingActor.setControlEnabled(false);
        mountCount++;
        controlEnabled = true;
    }
    
    public void unmount() {
        if (mountingActor != null) {
            mountingActor.mountedActor = null;
            mountingActor.setControlEnabled(true);
            mountingActor.wx = wx;
            mountingActor.wz = wz + 0.01;
            mountingActor.lastValidWx = lastValidWx;
            mountingActor.lastValidWz = lastValidWz;
            mountingActor = null;
        }
        control = Control.NONE;
        mountTimeout = Util.getTime() + 8000 + Util.random(5000);
        if (mountCount >= maxMountCount) {
            stateManager.switchTo("runaway");
        }
        else {
            stateManager.switchTo("resting");
        }
    }

    public void tryNextLife() {
        if (mountedActor != null) {
            mountedActor.unmount();
        }
        if (lives > 1) {
            lives--;
            deathCount++;
            energy = MAX_ENERGY;
            alive = true;
            wx = lastValidWx;
            wz = lastValidWz;
            wy = Terrain.getHeight(wx, wz);
            updateMovement();
            stateManager.switchTo("walking");
            setInvincible(3000);
        }
        else {
            alive = false;
            if (credits == 0) {
                destroy();
            }
        }
    }
    
    public int getUsedLives() {
        return deathCount + 1;
    }
    
    private String[] walkToData;
    
    public void walkTo(
            double wx, double wz, String nextState, Direction direction) {
        
        
        if (mountedActor != null) {
            mountedActor.walkTo(wx, wz, nextState, direction);
        }
        else {
            if (walkToData == null) {
                walkToData = new String[14];
            }
            walkToData[10] =  "" + wx;
            walkToData[11] =  "" + wz;
            walkToData[12] = "" + direction;
            walkToData[13] = nextState;
            WalkTo walkTo = (WalkTo) stateManager.getState("walk_to");
            walkTo.spawn(walkToData);
            stateManager.switchTo("walk_to");
        }
    }
    
    public void cancelWalkTo() {
        if (mountedActor != null) {
            mountedActor.getStateManager().switchTo("walking");
        }
        else {
            stateManager.switchTo("walking");
        }
    }
    
    public void playAnimatorFrame(AnimatorFrame playingFrame) {
        PlayingAnimator playingAnimator = 
                (PlayingAnimator) stateManager.getState("playing_animator");
        
        playingAnimator.setPlayingFrame(playingFrame);
        stateManager.switchTo("playing_animator");
    }
    
    @Override
    public void onDestroy() {
        Camera.removeLockingActor(this);
    }

    public void onDeathHeight() {
    }

    public boolean isInvincible() {
        return invincible;
    }
    
    public void setInvincible(long duration) {
        invincible = true;
        invincibleTimeout = Util.getTime() + duration;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void addPotion(Item potion) {
        if (potions.size() < maxPotions) {
            potions.add(potion);
            onPotionAdded(potion);
        }
    }

    public void onPotionAdded(Item potion) {
    }
    
    public double getWx() {
        return wx;
    }

    public void setWx(double wx) {
        this.wx = wx;
    }

    public double getWy() {
        return wy;
    }

    public void setWy(double wy) {
        this.wy = wy;
    }

    public double getWz() {
        return wz;
    }

    public void setWz(double wz) {
        this.wz = wz;
    }

    public double getLastValidWx() {
        return lastValidWx;
    }

    public double getLastValidWz() {
        return lastValidWz;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public double getVz() {
        return vz;
    }

    public void setVz(double vz) {
        this.vz = vz;
    }

    public double getWalkSpeed() {
        return walkSpeed;
    }

    public double getRunSpeed() {
        return runSpeed;
    }

    public double getJumpWalkSpeed() {
        return jumpWalkSpeed;
    }

    public double getJumpRunSpeed() {
        return jumpRunSpeed;
    }

    public int getCollisionMargin() {
        return collisionMargin;
    }

    public Control getControl() {
        return control;
    }

    public void setControl(Control control) {
        this.control = control;
    }

    public boolean isControlEnabled() {
        if (mountedActor != null) {
            return mountedActor.controlEnabled;
        }
        else {
            return controlEnabled;
        }
    }

    public void setControlEnabled(boolean controlEnabled) {
        if (mountedActor != null) {
            mountedActor.setControlEnabled(controlEnabled);
        }
        else {
            this.controlEnabled = controlEnabled;
        }
    }

    public StateManager<Actor, StateManager> getStateManager() {
        return stateManager;
    }

    public AnimationPlayer getAnimationPlayer() {
        return animationPlayer;
    }

    public String getLockingCameraId() {
        return lockingCameraId;
    }

    public void setLockingCameraId(String lockingCameraId) {
        this.lockingCameraId = lockingCameraId;
    }

    public boolean isLockingCamera() {
        return lockingCameraId != null;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isCollisionCheckEnabled() {
        return collisionCheckEnabled;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isOnFloor() {
        if (mountedActor != null) {
            return mountedActor.onFloor;
        }
        else {
            return onFloor;
        }
    }

    public double getFloorHeight() {
        return floorHeight;
    }

    public boolean isJumping() {
        if (mountedActor != null) {
            return mountedActor.jumping;
        }
        else {
            return jumping;
        }
    }
    
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isBlinking() {
        return blinking;
    }

    public void setBlinking(boolean blinking) {
        this.blinking = blinking;
    }

    public double getLastHitDistance() {
        return lastHitDistance;
    }

    public void setLastHitDistance(double lastHitDistance) {
        this.lastHitDistance = lastHitDistance;
    }

    public boolean isFireControlEnabled() {
        return fireControlEnabled;
    }

    public boolean isFire2ControlEnabled() {
        return fire2ControlEnabled;
    }

    public double getSquaredDistance(Actor other) {
        double dx = other.wx - wx;
        double dz = other.wz - wz;
        double dy = Terrain.getHeight(other.wx, other.wz) 
                                        - Terrain.getHeight(wx, wz);
                
        return dx * dx + dy * dy + dz * dz;
    }
    
    public boolean isMagicHittable() {
        if (stateManager == null || stateManager.getCurrentState() == null) {
            return false;
        }
        else {
            String currentState = stateManager.getCurrentState().getName();
            return isAlive() && !isDestroyed() 
                && !currentState.equals("wait_until_actors_death")
                    && !currentState.equals("appearance")  
                    && !currentState.equals("resting")  
                        && !currentState.equals("dead") 
                            && !currentState.equals("dying")
                                && !currentState.equals("none");
        }
    }

    public boolean isBoss() {
        return boss;
    }
    
    public void resetControls() {
        walkLeftControlEnabled = false;
        walkRightControlEnabled = false;
        runLeftControlEnabled = false;
        runRightControlEnabled = false;
        upControlEnabled = false;
        downControlEnabled = false;
        fireControlEnabled = false;
        fire2ControlEnabled = false;
        jumpControlEnabled = false;
        magicControlEnabled = false;
        projectileControlEnabled = false;
    }

    protected void knockDown(Actor sourceActor) {
        Audio.playSound("hit_knock_down");
        vx = sourceActor.getDirection().getDx() * 2.0;
        stateManager.switchTo("knock_down");
        sourceActor.setVx(0.25);
    }
    
    @Override
    public void fixedUpdate() {
        if (paused) {
            return;
        }
        
        resetControls();

        if (invincible && Util.getTime() >= invincibleTimeout) {
            invincible = false;
        }
        
        if (alive) {
            if (checkDeathHeight) {
                if (wy >= Terrain.getDeathHeight()
                        || wy >= Terrain.MAX_HEIGHT) {
                    
                    alive = false;
                    energy = 0;
                    stateManager.switchTo("none");
                    onDeathHeight();
                }
                if (destroyIfDeathHeight 
                        && wy >= Terrain.getDeathHeight() && !isDestroyed()) {
                    
                    alive = false;
                    destroy();
                }
            }
        }
        else {
            if (destroyIfNotAlive && !isDestroyed()) {
                destroy();
            }
        }
        
        stateManager.fixedUpdate();
    }
    
    public void updateControl() {
        if (!controlEnabled) {
            return;
        }
        if (stage.getManager().isTransiting()) {
            return;
        }
        switch (control) {
            case PLAYER_1 -> updateControlPlayer1();
            case PLAYER_2 -> updateControlPlayer2();
            case IA -> updateControlIA();
        }
    }

    public void updateControlPlayer1() {
        if (Input.isKeyDoublePressed(KEY_PLAYER_1_LEFT)) {
            runLeftControlEnabled = true;
            walkLeftControlEnabled = false;
        }
        else if (Input.isKeyDoublePressed(KEY_PLAYER_1_RIGHT)) {
            runRightControlEnabled = true;
            walkRightControlEnabled = false;
        }
        else if (Input.isKeyPressed(KEY_PLAYER_1_LEFT)) {
            runLeftControlEnabled = false;
            walkLeftControlEnabled = true;
        }
        else if (Input.isKeyPressed(KEY_PLAYER_1_RIGHT)) {
            runRightControlEnabled = false;
            walkRightControlEnabled = true;
        }
        else {
            runLeftControlEnabled = false;
            runRightControlEnabled = false;
        }
        if (Input.isKeyPressed(KEY_PLAYER_1_UP)) {
            upControlEnabled = true;
        }
        else if (Input.isKeyPressed(KEY_PLAYER_1_DOWN)) {
            downControlEnabled = true;
        }
        if (Input.isKeyJustPressed(KEY_PLAYER_1_MAGIC)) {
            magicControlEnabled = true;
        } 
        if (Input.isKeyJustPressed(KEY_PLAYER_1_ATTACK)) {
            fireControlEnabled = true;
        } 
        if (Input.isKeyJustPressed(KEY_PLAYER_1_JUMP) 
                && (int) wy == Terrain.getHeight(wx, wz)) {

            jumpControlEnabled = true;
        }
    }    

    public void updateControlPlayer2() {
        if (Input.isKeyDoublePressed(KEY_PLAYER_2_LEFT)) {
            runLeftControlEnabled = true;
            walkLeftControlEnabled = false;
        }
        else if (Input.isKeyDoublePressed(KEY_PLAYER_2_RIGHT)) {
            runRightControlEnabled = true;
            walkRightControlEnabled = false;
        }
        else if (Input.isKeyPressed(KEY_PLAYER_2_LEFT)) {
            runLeftControlEnabled = false;
            walkLeftControlEnabled = true;
        }
        else if (Input.isKeyPressed(KEY_PLAYER_2_RIGHT)) {
            runRightControlEnabled = false;
            walkRightControlEnabled = true;
        }
        else {
            runLeftControlEnabled = false;
            runRightControlEnabled = false;
        }
        if (Input.isKeyPressed(KEY_PLAYER_2_UP)) {
            upControlEnabled = true;
        }
        else if (Input.isKeyPressed(KEY_PLAYER_2_DOWN)) {
            downControlEnabled = true;
        }
        if (Input.isKeyJustPressed(KEY_PLAYER_2_MAGIC)) {
            magicControlEnabled = true;
        } 
        if (Input.isKeyJustPressed(KEY_PLAYER_2_ATTACK)) {
            fireControlEnabled = true;
        } 
        if (Input.isKeyJustPressed(KEY_PLAYER_2_JUMP) 
                && (int) wy == Terrain.getHeight((int) wx, (int) wz)) {

            jumpControlEnabled = true;
        }
    }
    
    protected void updateControlIA() {
    }
    
    protected void updateVelocity(boolean horizontalEnabled
            , boolean depthEnabled, boolean jumpEnabled
                , boolean jumping, boolean updateDirection) {
        
        vx = 0.0;
        vz = 0.0;

        if (horizontalEnabled) {
            if (runLeftControlEnabled 
                    || (running && walkLeftControlEnabled)) {
                
                vx = jumping ? -walkSpeed : -runSpeed;
                if (updateDirection) {
                    direction = Direction.LEFT;
                }
            }
            else if (runRightControlEnabled
                    || (running && walkRightControlEnabled)) {
            
                vx = jumping ? walkSpeed : runSpeed;
                if (updateDirection) {
                    direction = Direction.RIGHT;
                }
            }
            else if (walkLeftControlEnabled) {
                vx = -walkSpeed;
                if (updateDirection) {
                    direction = Direction.LEFT;
                }
            }
            else if (walkRightControlEnabled) {
                vx = walkSpeed;
                if (updateDirection) {
                    direction = Direction.RIGHT;
                }
            }
        }

        if (depthEnabled) {
            if (upControlEnabled) {
                vz = -walkSpeed;
            }
            else if (downControlEnabled) {
                vz = walkSpeed;
            }
        }

        if (jumpEnabled && jumpControlEnabled
                && (int) wy == Terrain.getHeight(wx, wz)) {
            
            vy = running ? -jumpRunSpeed : -jumpWalkSpeed;
        }        
    }
    
    protected void updateSlipOnTheEdges() {
        
        // left to right
        if (Terrain.getHeight(wx - collisionMargin, wz) 
                < Terrain.getHeight(wx + collisionMargin, wz)) {
            
            int mHeight = Terrain.getHeight((int) (wx - collisionMargin), (int) wz);
            
            int mChange = -collisionMargin;
            for (int m = -collisionMargin + 1; m <= collisionMargin; m++) {
                if (mHeight != Terrain.getHeight(wx + m, wz)) {
                    mChange = m;
                    break;
                }
            }
            //System.out.println("mChange = " + mChange);
            if (vy == 0 && vx == 0.0 && mChange < 0 && mChange > -8) {
                vx = 1.0;
            }
        }
        else {
            // right to left
            int mHeight = Terrain.getHeight(wx + collisionMargin, wz);
            int mChange = collisionMargin;
            for (int m = collisionMargin - 1; m >= -collisionMargin; m--) {
                if (mHeight != Terrain.getHeight(wx + m, wz)) {
                    mChange = m;
                    break;
                }
            }
            //System.out.println("mChange = " + mChange);
            if (vy == 0 && vx == 0.0 && mChange > 0 && mChange < 8) {
                vx = -1.0;
            }
        }
    }
    
    protected void updateMovement() {
        updateMovement(true, true);
    }
    
    protected void updateMovement(boolean horizontal, boolean vertical) {
        if (horizontal) {
            updateMovementHorizontal();
        }
        if (vertical) {
            updateMovementVertical();
        }
    }

    protected boolean isHorizontalMovementByCameraLimited() {
        return control == PLAYER_1 || control == PLAYER_2;
    }
    
    protected void updateMovementHorizontal() {
        updateSlipOnTheEdges();
        
        int maxV = (int) Math.ceil(Math.max(Math.abs(vx), Math.abs(vz)));
        if (maxV != 0) {
            double vxTmp = vx / maxV;
            double vzTmp = vz / maxV;
            for (int i = 0; i < maxV; i++) {
                double tmpX = wx + vxTmp;
                double tmpZ = wz + vzTmp;

                int leftHeight = Terrain.getHeight(
                        (int) (tmpX - collisionMargin), (int) wz);
                
                int rightHeight = Terrain.getHeight(
                        (int) (tmpX + collisionMargin), (int) wz);
                
                if (Terrain.isWalkable(
                        (int) (tmpX + collisionMargin), (int) wz) 
                    && Terrain.isWalkable(
                        (int) (tmpX - collisionMargin), (int) wz) 
                    && (wy <= rightHeight && wy <= leftHeight)
                    && (rightHeight > -1024 && leftHeight > -1024)) {

                    if (!isHorizontalMovementByCameraLimited()) {
                        wx = tmpX;
                    } 
                    else if ((vxTmp < 0 && tmpX >= Camera.getPlayerMinX() + horizontalCameraLimitMarginX) 
                            || (vxTmp > 0 && tmpX <= Camera.getPlayerMaxX() - horizontalCameraLimitMarginX)) {
                        
                        wx = tmpX;
                    }
                }
                else {
                    vxTmp = 0;
                }

                leftHeight = Terrain.getHeight(
                            (int) (wx - collisionMargin), (int) tmpZ);
                
                rightHeight = Terrain.getHeight(
                            (int) (wx + collisionMargin), (int) tmpZ);
                
                if (Terrain.isWalkable(
                        (int) (wx + collisionMargin), (int) tmpZ) 
                    && Terrain.isWalkable(
                        (int) (wx - collisionMargin), (int) tmpZ) 
                    && (wy <= rightHeight && wy <= leftHeight)
                    && (rightHeight > -1024 && leftHeight > -1024)) {

                    if (!isHorizontalMovementByCameraLimited()
                        || (floorHeight <= Terrain.MAX_HEIGHT 
                        && (tmpZ + floorHeight) >= Camera.getPlayerMinZ() 
                        && (tmpZ + floorHeight) <= Camera.getPlayerMaxZ())) {
                        
                        wz = tmpZ;
                    }
                }
                else {
                    vzTmp = 0;
                }
            }
        }
        
        updateLastValidPosition();
    }
    
    // save the last valid position to be used lster for revive
    private void updateLastValidPosition() {
        if (!isOnFloor()) {
            return;
        }
        
        double[] tl = { wx - 4.0, wz - 0.0,
                        //wx - 4.0, wz + 4.0,
                        //wx + 4.0, wz + 4.0,
                        wx + 4.0, wz - 0.0 };
                
        boolean locationValid = true;
        for (int i = 0; i < tl.length; i += 2) {
            if (!Terrain.isWalkable(tl[i], tl[i + 1]) 
                    || Terrain.getHeight(tl[i], tl[i + 1]) 
                        >= Terrain.getDeathHeight()) {
                
                locationValid = false;
                break;
            }
        }
        if (locationValid) {
            lastValidWx = wx;
            lastValidWz = wz;
        }
        if (mountingActor != null) {
            mountingActor.lastValidWx = lastValidWx;
            mountingActor.lastValidWz = lastValidWz;
        }
    }
    
    protected void updateMovementVertical() {
        vy += Settings.GRAVITY;
        wy += vy;
        if (vy >= 0.0 && wy >= Terrain.getHeight(wx + collisionMargin, wz)) {
            wy = Terrain.getHeight(wx + collisionMargin, wz);
            vy = 0.0;
            onFloor = true;
        }
        else if (vy >= 0.0 
                && wy >= Terrain.getHeight(wx - collisionMargin, wz)) {
            
            wy = Terrain.getHeight(wx - collisionMargin, wz);
            vy = 0.0;
            onFloor = true;
        }
        else {
            onFloor = false;
            //System.out.println("falling + wy = " + wy);
        }
        
        floorHeight = Math.min(Terrain.getHeight(wx + collisionMargin, wz)
                            , Terrain.getHeight(wx - collisionMargin, wz));        
    }
    
    public void setCorrectJumpHeight() {
        jumpHeight = floorHeight - wy;
    }
    
    @Override
    public void drawShadow(
            Offscreen offscreen, int cameraX, int cameraY, double shear) {
        
        if (jumpHeightInterpolationEnabled) {
            double jumpHeightTarget = floorHeight - wy;
            double jumpHeightDif = jumpHeightTarget - jumpHeight;
            if (jumpHeightDif > 1.0) {
                jumpHeightDif = 0.04 * jumpHeightDif;
            }
            jumpHeight = jumpHeight + jumpHeightDif;
        }
        if (animationPlayer != null) {
            animationPlayer.drawShadow(offscreen, wx, wy, wz
                        , cameraX, cameraY, direction, jumpHeight, shear);
        }
    }

    @Override
    public void draw(Graphics2D g, int cameraX, int cameraY) {
        
        if (invincible) {
            boolean blink = (int) (System.nanoTime() * 0.00000005) % 3 > 0;
            if (blink) {
                stateManager.draw(g, wx, wy, wz, cameraX, cameraY
                    , direction, blinking && !stage.manager.isTransiting()
                        , spriteScaleX, spriteScaleY, spriteAngle);
            }
        }
        else {
            stateManager.draw(g, wx, wy, wz, cameraX, cameraY
                    , direction, blinking && !stage.manager.isTransiting()
                        , spriteScaleX, spriteScaleY, spriteAngle);
        }
    }

    public List<Collider> getCurrentFrameColliders() {
        Frame currentFrame = animationPlayer
                                .getCurrentAnimation().getCurrentFrame();

        return currentFrame.colliders;
    }
    
    public void convertColliderToCameraSpace(
            Collider localCollider, Collider screenCollider
                , double cameraX, double cameraY) {
        
        localCollider.convertToCameraSpace(screenCollider
                        , wx, wy, wz, cameraX, cameraY, direction);
    }

    protected long getTime() {
        return System.currentTimeMillis();
    }
    
    public void onCollision(Collider thisCollider
                    , Collider otherCollider, Actor collidedActor) {
        
        if (mountingActor != null) {
            mountingActor.onCollision(
                    thisCollider, otherCollider, collidedActor);
        }
    }

    public void onHit(
            Reaction reaction, Collider sourceCollider, Actor sourceActor) {
    }
    
    public void react(
            Reaction reaction, Collider sourceCollider, Actor sourceActor) {
        
        boolean hit = reaction == STUNNED_1 || reaction == STUNNED_2 
                            || reaction == KNOCK_DOWN; 
        
        if (hit) {
            onHit(reaction, sourceCollider, sourceActor);
        }
        
        // if mounted
        if (mountedActor != null && hit) {
            mountedActor.unmount();
            hit(sourceCollider.getDamage());
            knockDown(sourceActor);
            return;
        }
        
        // normal other reactions
        switch (reaction) {
            case ATTACK_SHORT:
                if (stateManager.getCurrentState().getName().equals("walking")
                        && stateManager.isStateAvailable("attack_short")) {
                    
                    stateManager.switchTo("attack_short");
                }
                break;
            case ATTACK_LONG:
                if (stateManager.getCurrentState().getName().equals("walking")
                        && stateManager.isStateAvailable("attack_long")) {
                    
                    stateManager.switchTo("attack_long");
                }
                break;
            case STUNNED_1:
                hit(sourceCollider.getDamage());
                if (energy == 0 || !onFloor) {
                    knockDown(sourceActor);
                }
                else {
                    stateManager.switchTo("stunned_1");
                }
                break;
            case STUNNED_2:
                hit(sourceCollider.getDamage());
                if (energy == 0 || !onFloor) {
                    knockDown(sourceActor);
                }
                else {
                    stateManager.switchTo("stunned_2");
                }
                break;
            case KNOCK_DOWN:
                hit(sourceCollider.getDamage());
                knockDown(sourceActor);
                break;
            case GRAB_AND_THROW:
                if (stateManager.isStateAvailable("grab_and_throw") 
                    && stateManager.getCurrentState()
                        .getName().equals("walking") && throwingActor == null) {
                    
                    throwingActor = sourceActor;
                    throwingActor.getStateManager().switchTo("thrown");
                    throwingActor.setWz(wz); 
                    wz += 0.0001; // just to ensure this actor will be in front
                    stateManager.switchTo("grab_and_throw");
                }
                break;
            case PICK_UP:
                sourceActor.pickUp(this);
                break;
            case MOUNT:
                String currentStateName = 
                                stateManager.getCurrentState().getName();
                
                if (stateManager.isStateAvailable("mounting") 
                    && (currentStateName.equals("walking")
                        || currentStateName.equals("walk_to"))
                            && sourceActor.mountingActor == null) {
                    
                    sourceActor.mount(Actor.this);
                    mountedActor = sourceActor;
                    stateManager.switchTo("mounting");
                }
            default:
        }
        //System.out.println("player " + index + " energy = " + energy);
    }
    
    public void pickUp(Actor actor) {
    }

    public void destroyIfOutOfViewLeft() {
        int spriteWidth = animationPlayer.getCurrentAnimation()
                .getCurrentFrame().sprites[0].getImage().getWidth();
        
        if (!isDestroyed() && wx < Camera.getX() - spriteWidth) {
            destroy();
        }
    }
    
    public void destroyIfOutOfViewRight() {
        int spriteWidth = animationPlayer.getCurrentAnimation()
                .getCurrentFrame().sprites[0].getImage().getWidth();
        
        if (!isDestroyed() 
                && wx > Camera.getX() + Settings.CANVAS_WIDTH + spriteWidth) {
            
            destroy();
        }
    }
    
    @Override
    public int compareTo(Actor o) {
        
        // ensure actors like chicken leg is 
        // updated and drawn before mouting actor
        double az = 0.0;
        if (mountedActor != null) {
            az += 0.000001;
        }
        if (mountingActor != null) {
            az -= 0.000001;
        }
        
        return (int) Math.signum((wz + az) - o.wz);
    }

    // --- states ---

    protected class ActorState extends State<Actor, StateManager> {
        
        protected String[] data;
        
        public ActorState(String name) {
            super(stateManager, name, Actor.this);
        }

        public void spawn(String[] data) {
            this.data = data;
        }
        
        @Override
        public void fixedUpdate() {
            animationPlayer.update();
        }
        
        @Override
        public void draw(Graphics2D g, double wx, double wy, double wz
            , int cameraX, int cameraY, Direction direction, boolean blink
                , double scaleX, double scaleY, double angle) {
            
            animationPlayer.draw(g, wx, wy, wz, cameraX, cameraY
                    , direction, blink, scaleX, scaleY, angle);
        }
        
    }
    
    protected class None extends ActorState {
        
        public None() {
            super("none");
        }

        @Override
        public void onEnter() {
        }

        @Override
        public void fixedUpdate() {
        }
        
        @Override
        public void draw(Graphics2D g) {
        }

        @Override
        public void draw(Graphics2D g, double wx, double wy, double wz
                , int cameraX, int cameraY, Direction direction, boolean blink
                    , double scaleX, double scaleY, double angle) {
        }
        
    }
    
    protected class Appearance extends ActorState {
        
        long startTime;
        boolean appearanceStarted;
        boolean soundPlayed;
        Direction originalDirection;
        boolean keepDirection;

        public Appearance() {
            super("appearance");
        }
        
        @Override
        public void onEnter() {
            soundPlayed = false;
            startTime = -1;
            appearanceStarted = false;
            animationPlayer.setAnimation("none");
            originalDirection = direction;
        }
        
        @Override
        public void fixedUpdate() {
            if (!appearanceStarted) {
                if (startTime < 0) {
                    startTime = Util.getTime() + Long.parseLong(data[10]);
                    return;
                }
                else if (!appearanceStarted && Util.getTime() >= startTime) {
                    animationPlayer.setAnimation("appearance");
                    Animation animation = 
                            animationPlayer.getAnimation("appearance");
                    
                    if (animation.isParameterAvailable("keep_direction")) {
                        keepDirection = Boolean.parseBoolean(
                                animation.getParameter("keep_direction"));
                    }
                    appearanceStarted = true;
                }
                else {
                    return;
                }
            }
            if (keepDirection) {
                direction = originalDirection;
            }
            vx = 0.0;
            vz = 0.0;
            updateMovement();
            animationPlayer.update();
            Animation animation = animationPlayer.getCurrentAnimation();
            double frameIndex = animation.getCurrentFrameIndex();
            int framesCount = animation.getFramesCount();
            if (frameIndex >= framesCount - 0.01) {
                stateManager.switchTo("walking");
            }
            // play associated sound
            if (!soundPlayed && animation.isParameterAvailable("sound")) {
                Audio.playSound(animation.getParameter("sound"));
                soundPlayed = true;
            }
        }
        
    }
    
    protected class StandBy extends ActorState {
        
        long walkTime = -1;
        
        public StandBy() {
            super("stand_by");
        }

        @Override
        public void onEnter() {
            vx = 0.0;
            vz = 0.0;
            animationPlayer.setAnimation("stand_by");
        }

        @Override
        public void fixedUpdate() {
            if (walkTime < 0) {
                walkTime = getTime() + Long.parseLong(data[10]);
            }
            updateMovement();
            animationPlayer.update();
            if (getTime() >= walkTime) {
                stateManager.switchTo("walking");
            }
        }
        
    }
    
    protected class Walking extends ActorState {
        
        long idleDelayTime;
        long idleTime;
        boolean jumpAvailable;
        boolean invertDirectionOnEnter;
        
        public Walking() {
            super("walking");
        }

        public void setInvertDirectionOnEnter(boolean invertDirectionOnEnter) {
            this.invertDirectionOnEnter = invertDirectionOnEnter;
        }

        private void updateIdleTime() {
            idleTime = System.currentTimeMillis() + idleDelayTime;            
        }
        
        @Override
        public void onEnter() {
            throwingActor = null;
            jumpAvailable = stateManager.isStateAvailable("jumping");
            running = false;
            runLeftControlEnabled = false;
            runRightControlEnabled = false;
            updateIdleTime();
            animationPlayer.setAnimation("walking_down");
            Animation animation = animationPlayer.getCurrentAnimation();
            String idleDelayTimeStr = animation.getParameter("idle_delay_time");
            if (idleDelayTimeStr != null) {
                idleDelayTime = Integer.parseInt(idleDelayTimeStr);
            }
            if (invertDirectionOnEnter) {
                direction = direction.getOpposite();
            }
        }

        @Override
        public void onExit() {
            invertDirectionOnEnter = false;
        }
        
        @Override
        public void fixedUpdate() {
            updateControl();
            if (fireControlEnabled && jumpControlEnabled) {
                stateManager.switchTo("attack_special");
                return;
            }
            if (fire2ControlEnabled 
                    && stateManager.isStateAvailable("attack_short")) {
                
                stateManager.switchTo("attack_short");
                return;
            }
            boolean updateDirection = 
                    control == PLAYER_1 || control == PLAYER_2;
            
            updateVelocity(true, true, jumpAvailable, false, updateDirection);
            if ((jumpControlEnabled || vy != 0.0) && jumpAvailable) {
                
                // jumping or falling
                stateManager.switchTo("jumping");
                return;
            }
            else if (magicControlEnabled && !stage.isBossDefeated()) {
                
                if (mountingActor != null 
                    && mountingActor.getStateManager()
                        .isStateAvailable("using_magic") 
                            && !mountingActor.potions.isEmpty()) {
                    
                    mountingActor.getStateManager().switchTo("using_magic");
                    mountingActor.setWy(wy);
                    mountCount--;
                    unmount();
                    getStateManager().fixedUpdate();
                }
                else if (stateManager.isStateAvailable("using_magic") 
                                                && !potions.isEmpty()) {
                    
                    stateManager.switchTo("using_magic");
                }
                return;
            }
            else if (projectileControlEnabled
                    && stateManager.isStateAvailable("attack_projectile")) {
                
                stateManager.switchTo("attack_projectile");
                return;
            }
            else if (fireControlEnabled 
                    && stateManager.isStateAvailable("attack_1")) {
                
                stateManager.switchTo("attack_1");
                return;
            }
            else if (runLeftControlEnabled || runRightControlEnabled) {
                stateManager.switchTo("running");
                return;
            }
            if (vx != 0 || vz != 0) {
                updateIdleTime();
                animationPlayer.update();
                animationPlayer.setAnimation(
                        vz >= 0 ? "walking_down" : "walking_up", false);
            }
            else if (getTime() >= idleTime) {
                animationPlayer.update();
                animationPlayer.setAnimation("idle", false);
            }            
            updateMovement();

                    
        }

    }

    protected class Running extends ActorState {
        
        boolean attacking;
        Direction attackingDirection;
        boolean jumpAvailable;
        
        public Running() {
            super("running");
        }

        @Override
        public void onEnter() {
            attacking = false;
            jumpAvailable = stateManager.isStateAvailable("jumping");
            running = true;
            animationPlayer.setAnimation("running");
        }

        @Override
        public void onExit() {
            jumping = false;
        }
        
        @Override
        public void fixedUpdate() {
            updateControl();
            if (attacking) {
                updateMovement();
                direction = attackingDirection;
                if (vy == 0.0) {
                    stateManager.switchTo("walking");
                }
            }
            else {
                Direction previousDirection = direction;
                updateVelocity(
                        true, false, jumpAvailable, false, true);
                
                double previousWx = wx;
                updateMovement();
                if (wx == previousWx || previousDirection != direction) {
                    stateManager.switchTo("walking");
                    return;
                }
                else if (fireControlEnabled 
                        && animationPlayer.isAnimationAvailable(
                                                    "running_attack")) {
                    
                    animationPlayer.setAnimation("running_attack");
                    vy = -3.0;
                    vx = direction.getDx() * (runSpeed + 0.5);
                    attacking = true;
                    attackingDirection = direction;
                    jumping = true;
                }
                else if ((jumpControlEnabled || vy > 0.0) 
                        && stateManager.isStateAvailable("jumping")) {
                    
                    stateManager.switchTo("jumping");
                    updateVelocity(true, false, false, true, true);
                    return;
                }
                animationPlayer.update();
            }
        }

    }
    
    protected class Jumping extends ActorState {
        
        boolean attacking;
        long attackWaitTime;
        long attackEndTime;
        
        public Jumping() {
            super("jumping");
        }

        @Override
        public void onEnter() {
            animationPlayer.setAnimation("jumping");
            animationPlayer.setCurrentAnimationFrameIndex(0.0);
            attacking = false;
            attackWaitTime = getTime() + 90;
            jumping = true;
        }

        @Override
        public void onExit() {
            jumping = false;
        }
        
        @Override
        public void fixedUpdate() {
            updateControl();
            updateVelocity(true, false, false, true, true);
            updateMovement();
            if (!attacking && fireControlEnabled 
                && getTime() >= attackWaitTime 
                    && animationPlayer.isAnimationAvailable("jumping_attack")) {
                
                attacking = true;
                attackEndTime = getTime() + 90;
                animationPlayer.setAnimation("jumping_attack");
                Audio.playSound("attack_swing");
            }
            else if (attacking && getTime() >= attackEndTime && running 
                && !animationPlayer.getCurrentAnimation().getName()
                    .equals("jumping_death_stab")
                        && animationPlayer
                            .isAnimationAvailable("jumping_death_stab")) {
                
                // Death Stab. Ref: 
                // https://strategywiki.org/wiki/Golden_Axe/Moves_and_Controls
                animationPlayer.setAnimation("jumping_death_stab");
            }
            else if (attacking && getTime() >= attackEndTime && !running) {
                attacking = false;
                animationPlayer.setAnimation("jumping");
                animationPlayer.setCurrentAnimationFrameIndex(1.0);
                attackWaitTime = getTime() + 90;
            }
            else if (!attacking && vy > 0.0) {
                animationPlayer.setAnimation("jumping");
                animationPlayer.setCurrentAnimationFrameIndex(1.0);
            }
            
            if (onFloor) {
                stateManager.switchTo("walking");
            } 
        }
        
    }

    protected class Attack extends ActorState {
        
        String attackId;
        String nextShortAttackAnimationId;
        String nextLongAttackAnimationId;
        int attackDistance = 32;
        boolean combo;
        double nextAttackInAdvance = 2.0;
        Direction attackDirection;
        String projectileDecorationId;
        AnimationPlayer projectileDecoration;
        int projectileDecorationStartFrame;
        int projectileDecorationEndFrame;
        boolean projectileDecorationVisible;
        Projectile projectile;
        int projectileFrame;
        boolean projectileLauched;

        final Point pTmp = new Point();
        
        public Attack(int attackIndex) {
            super("attack_" + attackIndex);
            this.attackId = attackIndex + "";
            nextShortAttackAnimationId = "attack_" + (attackIndex + 1);
            nextLongAttackAnimationId = "attack_" + (attackIndex + 1);
            tryToCreateProjectile();
        }

        public Attack(String attackId) {
            super("attack_" + attackId);
            this.attackId = attackId;
            nextShortAttackAnimationId = "_empty_";
            nextLongAttackAnimationId = "_empty_";
            tryToCreateProjectile();
        }
        
        private void tryToCreateProjectile() {
            Animation animation = 
                    animationPlayer.getAnimation("attack_" + attackId);

            String projectileIdStr = 
                    animation.getParameter("projectile_id");
            
            if (projectile == null && projectileIdStr != null) {
                String projectileFrameStr = 
                        animation.getParameter("projectile_frame");
    
                projectile = new Projectile(stage
                        , projectileIdStr, projectileIdStr, projectileIdStr);
                
                projectile.destroy();
                projectileFrame = Integer.parseInt(projectileFrameStr);
            }
        }

        public Projectile getProjectile() {
            return projectile;
        }
        
        @Override
        public void onEnter() {
            combo = false;
            lastHitDistance = -1;
            attackDirection = direction;
            String previousAnimationId = 
                    animationPlayer.getCurrentAnimation().getName();
            
            animationPlayer.setAnimation("attack_" + attackId);
            Animation animation = animationPlayer.getCurrentAnimation();
            
            String nextAttackInAdvanceStr = 
                    animation.getParameter("next_attack_in_advance");
            
            if (nextAttackInAdvanceStr != null) {
                nextAttackInAdvance = 
                        Double.parseDouble(nextAttackInAdvanceStr);
            }

            String attackDistanceStr = 
                    animation.getParameter("attack_distance");
            
            if (attackDistanceStr != null) {
                attackDistance = Integer.parseInt(attackDistanceStr);
            }
            String nextAnimationStr = animation.getParameter("next_attack");
            if (nextAnimationStr != null) {
                nextShortAttackAnimationId = nextAnimationStr;
                nextLongAttackAnimationId = nextAnimationStr;
            }
            String nextShortAnimationStr = 
                    animation.getParameter("next_short_attack");
            
            if (nextShortAnimationStr != null) {
                nextShortAttackAnimationId = nextShortAnimationStr;
            }
            String nextLongAnimationStr = 
                    animation.getParameter("next_long_attack");
            
            if (nextLongAnimationStr != null) {
                nextLongAttackAnimationId = nextLongAnimationStr;
            }

            // spawn projectile
            projectileLauched = false;

            if (projectile != null && !projectile.isDestroyed()) {
                stateManager.switchTo("walking");
                animationPlayer.setAnimation(previousAnimationId);
                return;
            }

            if (projectileDecoration == null 
                    && animation.isParameterAvailable(
                                            "projectile_decoration_id")) {
                
                projectileDecorationId = 
                        animation.getParameter("projectile_decoration_id");
                
                projectileDecoration = 
                        Resource.getAnimationPlayer("decorations", null);
            }

            if (animation.isParameterAvailable(
                    "projectile_decoration_start_frame")) {
                
                projectileDecorationStartFrame = Integer.parseInt(
                        animation.getParameter(
                                "projectile_decoration_start_frame"));
            }
            
            if (animation.isParameterAvailable(
                    "projectile_decoration_end_frame")) {
                
                projectileDecorationEndFrame = Integer.parseInt(
                        animation.getParameter(
                                "projectile_decoration_end_frame"));
            }
            
            if (projectileDecoration != null) {
                projectileDecoration.setAnimation(projectileDecorationId);
            }

            // play associated sound
            if (animation.isParameterAvailable("sound")) {
                Audio.playSound(animation.getParameter("sound"));
            }
        }

        @Override
        public void onExit() {
            interruptProjectileIfNecessary();
        }
        
        // interrupt projectile if you get hit. for instance: blue dragon flame
        private void interruptProjectileIfNecessary() {
            String projectileInterruptibleStr = animationPlayer
                .getCurrentAnimation().getParameter("projectile_interruptible");

            if (projectileInterruptibleStr != null
                    && Boolean.parseBoolean(projectileInterruptibleStr)) {

                if (projectile != null) {
                    projectile.destroy();
                }
            }
        }        
        
        @Override
        public void fixedUpdate() {
            updateControl();
            direction = attackDirection;
            animationPlayer.update();
            Animation animation = animationPlayer.getCurrentAnimation();
            double frameIndex = animation.getCurrentFrameIndex();

            if (projectileDecoration != null) {
                projectileDecoration.update();
                projectileDecorationVisible = 
                        frameIndex >= projectileDecorationStartFrame 
                            && frameIndex <= projectileFrame;
            }
            
            if (projectile != null && !projectileLauched 
                                        && frameIndex >= projectileFrame) {
                
                FramePoint point = animation
                        .getCurrentFrame().getPoint(projectile.getId());
                
                point.convertPointToCameraSpace(pTmp, 0, 0, 0, 0, 0, direction);
                projectileLauched = true;
                Actor projectileOwner = 
                        mountingActor != null ? mountingActor : Actor.this;
                
                projectile.spawn(projectileOwner, direction
                                    , wx + pTmp.x, wy + pTmp.y, wz);
                
                projectile.setCorrectJumpHeight();
            }
            
            int framesCount = animation.getFramesCount();
            if (frameIndex >= framesCount - 0.01) {
                if ("true".equals(animation.getParameter(
                            "invert_direction_when_finished"))) {
                    
                    Walking walkingState = 
                            (Walking) stateManager.getState("walking");
                    
                    walkingState.setInvertDirectionOnEnter(true);
                }
                stateManager.switchTo("walking");
                return;
            }
            else if (frameIndex >= framesCount - (nextAttackInAdvance - 1.0) 
                                        && combo && lastHitDistance >= 0.0) {
                
                //System.out.printf("lastHitDistance=%f, attackDistance=%d \n"
                //                           , lastHitDistance, attackDistance);
                
                if (lastHitDistance < attackDistance) {
                    if (stateManager.isStateAvailable(
                            nextShortAttackAnimationId)) {
                        
                        stateManager.switchTo(nextShortAttackAnimationId);
                    }
                }
                else {
                    if (stateManager.isStateAvailable(
                            nextLongAttackAnimationId)) {
                        
                        stateManager.switchTo(nextLongAttackAnimationId);
                    }
                }
            }
            else if (frameIndex >= framesCount - nextAttackInAdvance 
                    && fireControlEnabled && jumpControlEnabled 
                        && stateManager.isStateAvailable("attack_special") ) {
                
                stateManager.switchTo("attack_special");
            }
            else if (frameIndex >= framesCount - nextAttackInAdvance 
                    && jumpControlEnabled 
                        && stateManager.isStateAvailable("jumping") ) {
                
                stateManager.switchTo("jumping");
                boolean updateDirection =
                        control == PLAYER_1 || control == PLAYER_2;
                
                updateVelocity(false, false, true, false, updateDirection);
            }
            else if (frameIndex >= framesCount - nextAttackInAdvance 
                    && magicControlEnabled 
                        && stateManager.isStateAvailable("magic")) {
                
                stateManager.switchTo("magic");
            }
            
            if (frameIndex >= framesCount - nextAttackInAdvance 
                    && fireControlEnabled) {
                
                combo = true;
            }
        }

        @Override
        public void draw(Graphics2D g, double wx, double wy, double wz
                , int cameraX, int cameraY, Direction direction, boolean blink
                    , double scaleX, double scaleY, double angle) {
            
            super.draw(g, wx, wy, wz, cameraX, cameraY
                    , direction, blink, scaleX, scaleY, angle); 

            if (projectileDecoration != null && projectileDecorationVisible) {
                FramePoint point = animationPlayer.getCurrentAnimation()
                        .getCurrentFrame().getPoint(projectileDecorationId);
                
                if (point != null) {
                    point.convertPointToCameraSpace(
                                        pTmp, 0, 0, 0, 0, 0, direction);

                    projectileDecoration.draw(
                        g, wx + pTmp.x, wy + pTmp.y, wz + 0.01, cameraX, cameraY
                            , direction, blink, scaleX, scaleY, angle);
                }
                
            }
        }
        
    }

    protected class AttackSpecial extends ActorState {
        
        boolean returning;
        double[] speeds = new double[10];
        double sign;
        double framesCount;
        int currentFrame;
        boolean discreteMovement;
                
        public AttackSpecial() {
            super("attack_special");
        }
        
        private void updateSpeeds() {
            Animation animation = animationPlayer.getCurrentAnimation();
            if (animation.isParameterAvailable("speeds")) {
                String[] params = animation.getParameter("speeds").split(",");
                for (int i = 0; i < params.length; i++) {
                    speeds[i] = Double.parseDouble(params[i]);
                }
            }
            else {
                for (int i = 0; i < speeds.length; i++) {
                    speeds[i] = 0.0;
                }
            }
            if (animation.isParameterAvailable("discrete_movement")) {
                discreteMovement = Boolean.parseBoolean(
                        animation.getParameter("discrete_movement"));
            }
            else {
                discreteMovement = false;
            }
        }
        
        @Override
        public void onEnter() {
            returning = false;
            animationPlayer.setAnimation("attack_special");
            updateSpeeds();
            framesCount = 
                    animationPlayer.getCurrentAnimation().getFramesCount();

            direction = direction.getOpposite();
            sign = 1.0;
            currentFrame = -1;
            vz = 0.0;
            Audio.playSound("attack_swing");
        }
        
        @Override
        public void fixedUpdate() {
            int possibleNextFrame = 
                    (int) animationPlayer.getCurrentAnimationFrameIndex();
            
            if (currentFrame != possibleNextFrame || !discreteMovement) {
                vx = direction.getDx() * sign * speeds[possibleNextFrame];
                updateMovementHorizontal();
                currentFrame = possibleNextFrame;
            }
            updateMovementVertical();
            animationPlayer.update();
            double frameIndex = animationPlayer.getCurrentAnimationFrameIndex();
            if (returning && frameIndex >= framesCount - 0.01) {
                stateManager.switchTo("walking");
            }
            else if (!returning && frameIndex >= framesCount - 0.01) {
                Audio.playSound("attack_swing");
                returning = true;
                sign = -1.0;
                animationPlayer.setAnimation("attack_special_return");
                updateSpeeds();
                framesCount = 
                        animationPlayer.getCurrentAnimation().getFramesCount();
            }
            else if (!returning && frameIndex >= framesCount - 1.01) {
                sign = 0.0;
            }
        }
        
    }
        
    protected class Stunned extends ActorState {
        
        int index;
        
        public Stunned(int index) {
            super("stunned_" + index);
            this.index = index;
        }

        @Override
        public void onEnter() {
            animationPlayer.setAnimation("stunned_" + index);
            
            // play associated sound
            Animation animation = animationPlayer.getCurrentAnimation();
            if (animation.isParameterAvailable("sound")) {
                Audio.playSound(animation.getParameter("sound"));
            }
        }
        
        @Override
        public void fixedUpdate() {
            animationPlayer.update();
            Animation animation = animationPlayer.getCurrentAnimation();
            double frameIndex = animation.getCurrentFrameIndex();
            if (frameIndex >= animation.getFramesCount() - 0.01) {
                stateManager.switchTo("walking");
            }
        }
        
    }

    protected class KnockDown extends ActorState {

        final Dust dust1;
        final Dust dust2;
        long standUpTime;
        double dustDx;
        double dustDz;
        String hitGroundSoundId = "hit_ground";
        
        public KnockDown() {
            super("knock_down");
            dust1 = new Dust(stage);
            dust2 = new Dust(stage);
        }

        @Override
        public void onEnter() {
            animationPlayer.setAnimation("knock_down");
            standUpTime = -2;
            // note: vx is set in the react() method
            vy = -4.0;
            vz = 0.0;
            dustDx = 0.0;
            dustDz = 0.0;
            Animation animation = animationPlayer.getCurrentAnimation();
            if (animation.isParameterAvailable("dust_dx")) {
                dustDx = Double.parseDouble(animation.getParameter("dust_dx"));
            }
            if (animation.isParameterAvailable("dust_dz")) {
                dustDz = Double.parseDouble(animation.getParameter("dust_dz"));
            }
            
            if (animation.isParameterAvailable("hit_ground_sound")) {
                hitGroundSoundId = animation.getParameter("hit_ground_sound");
            }
        }
        
        @Override
        public void fixedUpdate() {
            double vyCopy = vy;
            updateMovement();
            if (vy == 0.0) {
                vy = -0.5 * vyCopy;
                vx *= 0.5;
                if (standUpTime < 0.0 && Math.abs(vy) < 0.1) {
                    standUpTime = getTime() + 500;
                    vx = 0.0;
                    vy = 0.0;
                }
                if (animationPlayer.getCurrentAnimationFrameIndex() == 0.0) {
                    animationPlayer.setCurrentAnimationFrameIndex(1.0);
                    Audio.playSound(hitGroundSoundId);
                    dust1.spawn(direction, wx + direction.getDx() * dustDx
                                                , wy, wz + dustDz, 0.1, 3.0);
                }
                else if (standUpTime == -2) {
                    dust2.spawn(
                        direction, wx + direction.getDx() * dustDx
                                                , wy, wz + dustDz, 0.2, 2.5);
                    
                    standUpTime = -1;
                }
            }
            
            if (standUpTime >= 0 && getTime() >= standUpTime && onFloor) {
                if (energy == 0) {
                    stateManager.switchTo("dying");
                }
                else {
                    stateManager.switchTo("stand_up");
                }
            }
        }
        
    }
    
    protected class StandUp extends ActorState {
        
        public StandUp() {
            super("stand_up");
        }
        
        @Override
        public void onEnter() {
            animationPlayer.setAnimation("stand_up");
            animationPlayer.setCurrentAnimationSpeed(0.15);
        }
        
        @Override
        public void fixedUpdate() {
            updateMovement();
            animationPlayer.update();
            Animation animation = animationPlayer.getCurrentAnimation();
            double frameIndex = animation.getCurrentFrameIndex();
            int framesCount = animation.getFramesCount();
            if (frameIndex >= framesCount - 0.01) {
                stateManager.switchTo("walking");
            }
        }
        
    }
    
    protected class GrabAndThrow extends ActorState {
        
        boolean hurled;
        final Animation.FramePoint grabPoint = 
                new Animation.FramePoint("tmp", null, 0, 0);
        
        public GrabAndThrow() {
            super("grab_and_throw");
        }
        
        @Override
        public void onEnter() {
            hurled = false;
            animationPlayer.setAnimation("grab_and_throw");
            animationPlayer.setCurrentAnimationSpeed(0.04);
        }
        
        @Override
        public void fixedUpdate() {
            animationPlayer.update();
            double frameIndex = animationPlayer.getCurrentAnimationFrameIndex();
            if (frameIndex < 1.0) {
                throwingActor.getAnimationPlayer()
                        .setCurrentAnimationFrameIndex(0.0);
                
                animationPlayer.getCurrentAnimation().getCurrentFrame()
                        .getPoint("grab").convertPointToCameraSpace(
                                        grabPoint, 0, 0, 0, 0, 0, direction);
                
                throwingActor.setWx(wx + grabPoint.x);
                throwingActor.setWy(wy + grabPoint.y);
                throwingActor.setCorrectJumpHeight();
            }
            else if (frameIndex < 2.0) {
                throwingActor.getAnimationPlayer()
                        .setCurrentAnimationFrameIndex(1.0);
                
                animationPlayer.getCurrentAnimation().getCurrentFrame()
                        .getPoint("grab").convertPointToCameraSpace(
                                        grabPoint, 0, 0, 0, 0, 0, direction);

                throwingActor.setWx(wx + grabPoint.x);
                throwingActor.setWy(wy + grabPoint.y);
                throwingActor.setCorrectJumpHeight();
            }
            else if (frameIndex < 2.99 && !hurled) {
                hurled = true;
                
                // avoid get out of valid terrain position
                throwingActor.setWx(wx); 
                
                throwingActor.vx = 24 * direction.getDx();
                throwingActor.updateMovement();
                
                throwingActor.vx = 2.25 * direction.getDx();
                //throwingActor.setWx( 
                //        throwingActor.getWx() + 24 * direction.getDx());
                throwingActor.getStateManager().switchTo("knock_down");
            }
            else if (frameIndex >= 2.99) {
                stateManager.switchTo("walking");
            }
        }
        
    }

    protected class Thrown extends ActorState {
        
        public Thrown() {
            super("thrown");
        }

        @Override
        public void onEnter() {
            animationPlayer.setAnimation("thrown");
        }
        
    }
    
    protected class Resting extends ActorState {
        
        long wakeupTime;
        
        public Resting() {
            super("resting");
        }
        
        @Override
        public void onEnter() {
            animationPlayer.setAnimation("resting");
            Animation animation = animationPlayer.getCurrentAnimation();
            if (animation.isParameterAvailable("wakeup_delay_time")) {
                long wakeupDelayTime = Long.parseLong(
                        animation.getParameter("wakeup_delay_time"));
                
                wakeupTime = getTime() + wakeupDelayTime 
                                        + (long) (Math.random() * 500);
            }
            else {
                wakeupTime = -1;
            }
        }

        @Override
        public void fixedUpdate() {
            vx = 0.0;
            vz = 0.0;
            animationPlayer.setAnimation("resting"); // reset reacted actors
            updateMovement();
            if (mountTimeout < 0) {
                mountTimeout = Util.getTime() + 8000 + Util.random(5000);
            }
            if (getTime() > mountTimeout 
                    && stateManager.isStateAvailable("runaway")) {
                
                stateManager.switchTo("runaway");
            }
            else if (wakeupTime >= 0 && getTime() >= wakeupTime) {
                stateManager.switchTo("stand_up");
            }
        }
        
    }

    protected class UsingMagic extends ActorState {

        Magic[] availableMagics;
        Magic magic;
        int step;
        boolean potionsConsumed;
        final List<Item> consumedPotions = new ArrayList<>();
        
        public UsingMagic(PlayerCharacter playerCharacter) {
            super("using_magic");
            availableMagics = new Magic[playerCharacter.maxPotions];
            switch (playerCharacter) {
                case AX ->{
                    Magic magicA = new VolcanoABC(stage, Actor.this, VOLCANO_A);
                    Magic magicB = new VolcanoABC(stage, Actor.this, VOLCANO_B);
                    Magic magicC = new VolcanoABC(stage, Actor.this, VOLCANO_C);
                    Magic magicD = new VolcanoD(stage, Actor.this);
                    magicA.create();
                    magicB.create();
                    magicC.create();
                    magicD.create();
                    availableMagics[0] = magicA;
                    availableMagics[1] = magicA;
                    availableMagics[2] = magicB;
                    availableMagics[3] = magicB;
                    availableMagics[4] = magicC;
                    availableMagics[5] = magicD;
                }
                case TYRIS ->{
                    Magic magicA = new FireA(stage, Actor.this);
                    Magic magicB = new FireB(stage, Actor.this);
                    Magic magicC = new FireC(stage, Actor.this);
                    Magic magicD = new FireD(stage, Actor.this);
                    Magic magicE = new FireE(stage, Actor.this);
                    Magic magicF = new FireF(stage, Actor.this);
                    magicA.create();
                    magicB.create();
                    magicC.create();
                    magicD.create();
                    magicE.create();
                    magicF.create();
                    availableMagics[0] = magicA;
                    availableMagics[1] = magicA;
                    availableMagics[2] = magicA;
                    availableMagics[3] = magicA;                    
                    availableMagics[4] = magicB;                    
                    availableMagics[5] = magicC;                    
                    availableMagics[6] = magicD;                    
                    availableMagics[7] = magicE;                    
                    availableMagics[8] = magicF;                    
                }
                case GILIUS ->{
                    Magic magicA = new LightningA(stage, Actor.this);
                    Magic magicB = new LightningB(stage, Actor.this);
                    Magic magicC = new LightningC(stage, Actor.this);
                    magicA.create();
                    magicB.create();
                    magicC.create();
                    availableMagics[0] = magicA;
                    availableMagics[1] = magicB;
                    availableMagics[2] = magicB;
                    availableMagics[3] = magicC;
                }
            }
        }
        
        @Override
        public void onEnter() {
            magic = availableMagics[potions.size() - 1];
            animationPlayer.setAnimation("magic");
            animationPlayer.setCurrentAnimationSpeed(0.04);
            animationPlayer.setCurrentAnimationLooping(false);
            step = 0;
            consumedPotions.clear();
            potionsConsumed = false;
            freezeAllRelatedActors();
        }
        
        @Override
        public void fixedUpdate() {
            switch (step) {
                case 0 -> {
                    animationPlayer.update();
                    double frameIndex = 
                            animationPlayer.getCurrentAnimationFrameIndex();
                    
                    if (frameIndex > 1.00 && !potionsConsumed) {
                        potionsConsumed = true;
                        consumeMagicPotions();
                    }
                    else if (frameIndex >= 1.99) {
                        Audio.playSound("magic_used");
                        stage.startBackgroundTransition(2, "magic", 0.05, 0);
                        step = 1;
                    }
                }
                case 1 -> {
                    if (stage.getBackground(MAGIC_LAYER).isFinished()) {
                        Camera.setHardShakeEnabled(true);
                        magic.use();
                        blinkAllDamagebleActors();
                        step = 2;
                    }
                }
                case 2 -> {
                    if (magic.isFinished()) {
                        Camera.setHardShakeEnabled(false);
                        stage.startBackgroundTransition(2, "empty", 0.05, 0);
                        releaseAllRelatedActors();
                        step = 3;
                    }
                }
                case 3 -> {
                        stateManager.switchTo("walking");
                        step = 4;
                }
            }
        }

        private void consumeMagicPotions() {
            int consumedPotionsCount = magic.getConsumedPotions();
            for (int i = 0; i < potions.size(); i++) {
                Item potion = potions.get(i);
                potion.disintegrate(i + 1, potions.size(), wx, wy, wz + 0.01);
                if (consumedPotionsCount > 0 && !potions.isEmpty()) {
                    consumedPotionsCount--;
                    consumedPotions.add(potion);
                }
            }
            potions.removeAll(consumedPotions);
            consumedPotions.clear();
        }
        
        private void freezeAllRelatedActors() {
            for (Actor actor : stage.getActors()) {
                if (actor instanceof Projectile) {
                    actor.destroy();
                }
                else if (actor != Actor.this) {
                    actor.setPaused(true);
                }
            }
            if (stage.getCurrentAnimator() != null) {
                stage.getCurrentAnimator().pause();
            }
        }
        
        private void blinkAllDamagebleActors() {
            for (Actor actor : stage.getActors()) {
                if (actor.isMagicHittable()) {
                    actor.setBlinking(actor.getControl() == IA);
                }
            }
        }
        
        private void releaseAllRelatedActors() {
            for (Actor actor : stage.getActors()) {
                actor.setBlinking(false);
                actor.setPaused(false);
                if (actor instanceof Thief 
                    || (actor instanceof Enemy enemy 
                        && enemy.isMagicHittable() && !enemy.isMountable())) {
                    
                    direction = actor.getWx() - wx > 0 ? RIGHT : LEFT;
                    actor.setDirection(direction.getOpposite());
                    actor.react(KNOCK_DOWN, magic.getCollider(), Actor.this);
                }
            }
            if (stage.getCurrentAnimator() != null) {
                stage.getCurrentAnimator().resume();
            }
        }
        
    }
    
    protected class Dying extends ActorState {
        
        long deadTime;
        
        public Dying() {
            super("dying");
        }
        
        @Override
        public void onEnter() {
            animationPlayer.setAnimation("dying");
            deadTime = Util.getTime() + 1000;
            
            // play associated sound
            Animation animation = animationPlayer.getCurrentAnimation();
            if (animation.isParameterAvailable("sound")) {
                Audio.playSound(animation.getParameter("sound"));
            }
        }
        
        @Override
        public void fixedUpdate() {
            animationPlayer.update();
            if (Util.getTime() > deadTime) {
                stateManager.switchTo("dead");
            }
        }
        
    }

    protected class Dead extends ActorState {
        
        long reviveTime;
        
        public Dead() {
            super("dead");
        }
        
        @Override
        public void onEnter() {
            alive = false;
            reviveTime = Util.getTime() + 1000;
            animationPlayer.setAnimation("dead");
            Camera.removeLockingActor(Actor.this);
        }
        
        @Override
        public void fixedUpdate() {
            if ((control == PLAYER_1 || control == PLAYER_2) 
                                && Util.getTime() >= reviveTime) {
                
                tryNextLife();
            }
            else {
                destroyIfOutOfViewLeft();
            }
        }
        
    }

    protected class Runaway extends ActorState {
        
        double maxJumpHeight = 48;
        double targetX;
        double targetZ;
        
        public Runaway() {
            super("runaway");
        }

        @Override
        public void onEnter() {
            double rz = 32 * Math.random() - 16;
            targetZ = wz + Math.signum(rz) * 16 + rz;
            targetX = Camera.getX() + 2 * Settings.CANVAS_WIDTH;
            if (Math.random() > 0.5) {
                targetX = Camera.getX() - Settings.CANVAS_WIDTH;
            }
            animationPlayer.setAnimation("runaway");
        }
        
        @Override
        public void fixedUpdate() {
            double difX = targetX - wx;
            double difZ = targetZ - wz;
            vx = 0.0;
            vz = 0.0;
            if (Math.abs(difX) >= 4.0) {
                vx = 5.0 * Math.signum(difX);
                direction = vx < 0.0 ? LEFT : RIGHT;
            }
            if (onFloor && Math.abs(difZ) >= 1.0) {
                vz = 1.0 * Math.signum(difZ);
            }
            avoidGetStuckOnTerrain();
            updateMovement();
            destroyIfOutOfViewLeft();
            destroyIfOutOfViewRight();
            animationPlayer.update();
        }

        private void avoidGetStuckOnTerrain() {
            double tmpX = wx + Math.signum(vx) 
                    * collisionMargin + Math.signum(vx);
            
            double tmpJumpHeight = Terrain.getHeight(tmpX, wz) - wy;
            
            if (!Terrain.isWalkable(tmpX, wz)
                    || (tmpJumpHeight < 0 
                        && Math.abs(tmpJumpHeight) > maxJumpHeight)) {
                
                targetX = Camera.getX() + 2 * Settings.CANVAS_WIDTH;
                if (vx > 0.0) {
                    targetX = Camera.getX() - Settings.CANVAS_WIDTH;
                }
                targetZ = wz + 128.0;
            }
            else if (onFloor && tmpJumpHeight < 0 
                    && Math.abs(tmpJumpHeight) <= maxJumpHeight) {
                
                vy = -5.0;
            }
        }
        
    }
    
    protected class Mounting extends ActorState {
        
        public Mounting() {
            super("mounting");
        }
        
        @Override
        public void onEnter() {
            animationPlayer.setAnimation("mounting");
            wz = mountedActor.getWz() + 0.000001; // ensure this actor in front
        }
        
        @Override
        public void fixedUpdate() {
            animationPlayer.update();
            double frameIndex = animationPlayer.getCurrentAnimationFrameIndex();
            if (frameIndex >= 0.99) {
                stateManager.switchTo("mounted");
                mountedActor.getStateManager().switchTo("walking");
            }
        }
        
    }
    
    protected class Mounted extends ActorState {

        final Animation.FramePoint mountPoint = 
                new Animation.FramePoint("tmp", null, 0, 0);
        
        public Mounted() {
            super("mounted");
        }

        @Override
        public void onEnter() {
            jumpHeightInterpolationEnabled = false;
        }

        @Override
        public void onExit() {
            jumpHeightInterpolationEnabled = true;
        }
        
        @Override
        public void fixedUpdate() {
            Frame mountedActorFrame = mountedActor.getAnimationPlayer()
                        .getCurrentAnimation().getCurrentFrame();
            
            FramePoint firstPoint = mountedActorFrame.getFirstPoint("mounted_");
            if (firstPoint != null) {
                animationPlayer.setAnimation(firstPoint.name);
                direction = mountedActor.getDirection();
                firstPoint.convertPointToCameraSpace(
                        mountPoint, 0, 0, 0, 0, 0, direction);
                
                wx = mountedActor.getWx() + mountPoint.x;
                wz = mountedActor.getWz() + 0.000001; // this actor in front
                if (mountedActor.isAlive()) {
                    wy = mountedActor.getWy() + mountPoint.y;
                }
                else {
                    wy++;
                }
                floorHeight = mountedActor.floorHeight;
                int footToCrotchDistance = animationPlayer
                        .getCurrentAnimation().getCurrentFrame().getSprite(0)
                            .getImage().getHeight() - mountPoint.y;
                
                jumpHeight = mountedActor.jumpHeight + footToCrotchDistance;
            }
        }
        
    }

    // ending rotating axe when death adder is defeated
    protected class GoldenAxe extends ActorState {
        
        boolean started;
        long nextSoundTime;
        boolean deadSoundPlayed;
        boolean finished;
        
        public GoldenAxe() {
            super("golden_axe");
        }
        
        @Override
        public void onEnter() {
            animationPlayer.setAnimation("none");
            started = false;
            finished = false;
        }
        
        @Override
        public void fixedUpdate() {
            Actor deathAdder = stage.getActorById("death_adder");
            //if (deathAdder != null && deathAdder.energy == 0 && !started) {
            if (!finished && deathAdder != null 
                    && !started && deathAdder.energy == 0) {
                
                vy = -5;
                wy = deathAdder.getWy() - 32;
                spriteAngle = Math.toRadians(
                        deathAdder.getDirection().getDx() * -270);
                
                animationPlayer.setAnimation("golden_axe");
                started = true;
            }
            if (!finished && deathAdder != null && started) {
                direction = deathAdder.getDirection();
                wx = deathAdder.getWx() - direction.getDx() * 32;
                wz = deathAdder.getWz() + 0.000001; //ensure this actor in front
                
                if (!deadSoundPlayed && vy > 0.0 
                        && Math.abs(wy - deathAdder.getWy()) < 70) {
                    
                    Audio.playSound("death_adder_defeated");
                    deadSoundPlayed = true;
                }
                if (vy < 0.0 || Math.abs(wy - deathAdder.getWy()) > 30) {
                    wy += vy;
                    vy += GRAVITY / 1.5;
                    spriteAngle -= direction.getDx() * 0.13;
                    if (Util.getTime() > nextSoundTime) {
                        Audio.playSound("golden_axe_rotating");
                        nextSoundTime = Util.getTime() + 475;
                    }
                }
                else {
                    finished = true;
                }
            }
        }
        
    }
    
    // example: command 20 SPAWN heninger purple heninger_0_b wave_0 350 380 
    //                  wait_until_actors_death 2 heninger_0 longmoan_0 walking
    protected class WaitUntilActorsDeath extends ActorState {
        
        public WaitUntilActorsDeath() {
            super("wait_until_actors_death");
        }

        @Override
        public void fixedUpdate() {
            boolean allActorsDeath = true;
            int enemiesLastIndex = 11 + Integer.parseInt(data[10]);
            for (int i = 11; i < enemiesLastIndex; i++) {
                Actor actor = stage.getActorById(data[i]);
                if (actor != null && actor.isAlive()) {
                    allActorsDeath = false;
                    break;
                }
            }
            if (allActorsDeath) {
                ActorState nextState = (ActorState) 
                        stateManager.getState(data[enemiesLastIndex]);
                
                int copySize = data.length - enemiesLastIndex;
                System.arraycopy(data, enemiesLastIndex, data, 9, copySize);
                nextState.spawn(data);
                stateManager.switchTo(data[9]);
            }
        }

        @Override
        public void draw(Graphics2D g, double wx, double wy, double wz
            , int cameraX, int cameraY, Direction direction, boolean blink
                , double scaleX, double scaleY, double angle) {
        }
        
    }

    protected class WalkTo extends ActorState {
        
        double targetX;
        double targetZ;
        String nextStateId;
        Direction finishedDirection;
        
        public WalkTo() {
            super("walk_to");
        }

        @Override
        public void onEnter() {
            targetX = Double.parseDouble(data[10]);
            targetZ = Double.parseDouble(data[11]);
            finishedDirection = null;
            if (!data[12].equals("null")) {
                finishedDirection = Direction.valueOf(data[12]);
            }
            nextStateId = data[13];
            running = false;
        }

        @Override
        public void fixedUpdate() {
            // wait until this actor touches the ground
            if (!isOnFloor()) {
                updateMovement();
                return;
            }
            animationPlayer.setAnimation("walking_down", false);
            
            int difX = (int) (targetX - wx);
            int difZ = (int) (targetZ - wz);
            boolean finished = true;
            if (Math.abs(difX) > walkSpeed) {
                if (difX > 0) {
                    walkRightControlEnabled = true;
                }
                if (difX < 0) {
                    walkLeftControlEnabled = true;
                }
                finished = false;
            }
            if (Math.abs(difZ) > walkSpeed) {
                if (difZ > 0) {
                    downControlEnabled = true;
                    animationPlayer.setAnimation("walking_down", false);
                }
                if (difZ < 0) {
                    upControlEnabled = true;
                    animationPlayer.setAnimation("walking_up", false);
                }
                finished = false;
            }
            animationPlayer.update();
            if (finished) {
                ActorState nextState = 
                        (ActorState) stateManager.getState(nextStateId);
                
                System.arraycopy(data, 13, data, 9, data.length - 13);
                nextState.spawn(data);
                stateManager.switchTo(nextStateId);
                if (finishedDirection != null) {
                    direction = finishedDirection;
                }
            }
            else {
                updateVelocity(true, true, false, false, true);
                updateMovement();
            }
        }
        
    }

    protected class ShowStartDialogText extends ActorState {
        
        static boolean finished;
        int textIndex;
        
        public ShowStartDialogText() {
            super("show_start_dialog_text");
        }

        @Override
        public void onEnter() {
            if (control == PLAYER_1) {
                textIndex = 0;
                String text = Resource.getText("start_dialog_text_0");
                Dialog.show(10, 7, 18, 4, text, true, DEFAULT_FONT_COLOR, null);
            }
            animationPlayer.setAnimation("walking_down");
            finished = false;
        }
        
        @Override
        public void fixedUpdate() {
            if (control == PLAYER_1) {
                Dialog.update();
                boolean keyJustPressed = Input.isKeyJustPressed(KEY_START_1) 
                        || Input.isKeyJustPressed(KEY_START_2) 
                            || Input.isKeyJustPressed(KEY_PLAYER_1_ATTACK);

                if (!Dialog.isFinished() && keyJustPressed) {
                    Dialog.showAll();
                }
                else if (Dialog.isFinished() 
                        && textIndex < 2 && keyJustPressed) {
                    
                    textIndex++;
                    String text = Resource.getText(
                                        "start_dialog_text_" + textIndex);

                    Dialog.show(
                        10, 7, 18, 4, text, true, DEFAULT_FONT_COLOR, null);
                }
                else if (Dialog.isFinished() && keyJustPressed) {
                    Dialog.hide();
                    stateManager.switchTo("walking");
                    finished = true;
                }
            }
            else if (control == PLAYER_2 && finished) {
                stateManager.switchTo("walking");
            }
        }
        
    }

    protected class PlayingAnimator extends ActorState {
        
        double animationFrame;
        
        public PlayingAnimator() {
            super("playing_animator");
        }

        @Override
        public void onEnter() {
        }

        @Override
        public void fixedUpdate() {
            if (animationFrame < 0) {
                animationPlayer.update();
            }
        }

        public void setPlayingFrame(AnimatorFrame playingFrame) {
            spriteScaleX = playingFrame.scaleX;
            spriteScaleY = playingFrame.scaleY;
            spriteAngle = playingFrame.angle;
            wx = playingFrame.wx;
            wy = playingFrame.wy;
            wz = playingFrame.wz;
            floorHeight = Terrain.getHeight(wx, wz); 
            setCorrectJumpHeight();
            animationPlayer.setAnimation(playingFrame.animationId, false);
            animationFrame = playingFrame.animationFrame;
            if (animationFrame >= 0) {
                animationPlayer.setCurrentAnimationFrameIndex(animationFrame);
            }
            if (playingFrame != null) {
                direction = playingFrame.direction;
            }
        }
        
    }
    
    protected class Demo extends ActorState {
        
        public Demo() {
            super("demo");
        }

        @Override
        public void onEnter() {
            manager.switchTo("using_magic");
        }

    }
    
}
