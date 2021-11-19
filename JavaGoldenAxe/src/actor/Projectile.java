package actor;

import infra.Actor;
import infra.Animation;
import infra.Audio;
import infra.Collider;
import infra.Direction;
import static infra.Direction.*;
import infra.GoldenAxeGame;
import static infra.Settings.*;
import infra.Terrain;
import infra.Util;
import scene.Stage;

/**
 * Projectile class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Projectile extends Actor {
    
    private Actor owner;
    private final String animationId;
    private Decoration[] trails;
    private int trailRandomX;
    private int trailRandomOffsetX;
    private int trailRandomZ;
    private int trailRandomOffsetZ;
    private long trailInterval;
    private long nextTrailTime;
    private String trailSoundId;
    private boolean hit;
    private boolean destroyIfHit;
    
    public Projectile(Stage stage, String projectileId
                                    , String resId, String animationId) {
        
        super(stage, projectileId, 0, 0, resId, null, null);
        this.animationId = animationId;
        checkDeathHeight = false;
        createTrailIfRequested();
        
        control = Control.NONE;
        stateManager.addState(new None());    
        stateManager.addState(new ProjectileState("animating", animationId));    
        if (animationPlayer.isAnimationAvailable("hit")) {
            stateManager.addState(new ProjectileState("hit", "hit"));    
        }
        stateManager.switchTo("none");
    }

    private void createTrailIfRequested() {
        //parameter death_adder_fire vx 3.0
        //parameter death_adder_fire try_hit_players true
        //parameter death_adder_fire trail_id death_adder_fire_trail        
        //parameter death_adder_fire trails_count 10

        // command 4830 SPAWN decoration decoration_id hole_red 4910 -191 330 
        //          0.1 false true 1000 wait_until_actors_death 1 longmoan_5
        
        Animation animation = animationPlayer.getAnimation(animationId);
        if (animation.isParameterAvailable("trail_id")) {
            int trailsCount = 1;
            if (animation.isParameterAvailable("trails_count")) {
                trailsCount = Integer.parseInt(
                        animation.getParameter("trails_count"));
            }
            if (animation.isParameterAvailable("trail_interval")) {
                trailInterval = Integer.parseInt(
                        animation.getParameter("trail_interval"));
            }
            if (animation.isParameterAvailable("trail_sound")) {
                trailSoundId = animation.getParameter("trail_sound");
            }
            
            trails = new Decoration[trailsCount];
            for (int i = 0; i < trailsCount; i++) {
                String[] dataTmp = new String[13];
                dataTmp[4] = id + "_projectile_decoration_" + i;
                dataTmp[5] = animation.getParameter("trail_id");
                dataTmp[6] = "0";
                dataTmp[7] = "0";
                dataTmp[8] = "0";
                dataTmp[9] = "0.1";
                dataTmp[10] = "false";
                dataTmp[11] = "false";
                dataTmp[12] = "0";
                trails[i] = new Decoration(stage, dataTmp);
                trails[i].setDestroyed(true);
            }
        
            trailRandomX = 3;
            trailRandomOffsetX = 1;
            trailRandomZ = 3;
            trailRandomOffsetZ = 0;
            if (animation.isParameterAvailable("trail_random_x")) {
                trailRandomX = Integer.parseInt(
                                    animation.getParameter("trail_random_x"));
            }
            if (animation.isParameterAvailable("trail_random_offset_x")) {
                trailRandomOffsetX = Integer.parseInt(
                            animation.getParameter("trail_random_offset_x"));
            }
            if (animation.isParameterAvailable("trail_random_z")) {
                trailRandomZ = Integer.parseInt(
                                    animation.getParameter("trail_random_z"));
            }
            if (animation.isParameterAvailable("trail_random_offset_z")) {
                trailRandomOffsetZ = Integer.parseInt(
                            animation.getParameter("trail_random_offset_z"));
            }
        }
    }

    public Actor getOwner() {
        return owner;
    }
    
    public void spawn(Actor owner, Direction direction
                                , double wx, double wy, double wz) {
        
        this.owner = owner;
        this.control = owner.getControl();
        this.direction = direction;
        this.wx = wx;
        this.wy = wy;
        this.wz = wz;
        stateManager.switchTo("animating");
        hit = false;
        destroyed = false;
        stage.addActor(this);
    }
    
    @Override
    public void onCollision(Collider thisCollider
                    , Collider otherCollider, Actor collidedActor) {
        
        if (owner instanceof Player player) {
            player.onCollision(thisCollider, otherCollider, collidedActor);
        }
        hit();
    }
    
    private void hit() {
        hit = true;
        if (stateManager.isStateAvailable("hit")) {
            stateManager.switchTo("hit");
        }
        else if (destroyIfHit) {
            destroy();
        }
    }

    
    // --- states ---
    
    public class ProjectileState extends ActorState {
        
        String stateAnimationId;
        double projectileVx;
        double projectileVy;
        double projectileVz;
        boolean tryToFollowPlayerZ;
        boolean checkHeightHit;
        boolean checkWalkableHit;
        boolean keepOnFloor;
        boolean affectedByGravity;
        
        public ProjectileState(String stateId, String animationId) {
            super(stateId);
            stateAnimationId = animationId;
        }

        public String getStateAnimationId() {
            return stateAnimationId;
        }

        public void setStateAnimationId(String stateAnimationId) {
            this.stateAnimationId = stateAnimationId;
        }

        @Override
        public void onEnter() {
            animationPlayer.setAnimation(stateAnimationId);
            Animation animation = animationPlayer.getCurrentAnimation();
            projectileVx = 0.0;
            projectileVy = 0.0;
            projectileVz = 0.0;
            if (animation.isParameterAvailable("vx")) {
                projectileVx = direction.getDx() 
                        * Double.parseDouble(animation.getParameter("vx"));
            }
            if (animation.isParameterAvailable("vy")) {
                projectileVy = Double.parseDouble(animation.getParameter("vy"));
            }
            if (animation.isParameterAvailable("vz")) {
                projectileVz = Double.parseDouble(animation.getParameter("vz"));
            }
            tryToFollowPlayerZ = false;
            if (animation.isParameterAvailable("try_to_follow_player_z")) {
                tryToFollowPlayerZ = Boolean.parseBoolean(
                        animation.getParameter("try_to_follow_player_z"));
            }
            checkWalkableHit = true;
            if (animation.isParameterAvailable("check_walkable_hit")) {
                checkWalkableHit = Boolean.parseBoolean(
                        animation.getParameter("check_walkable_hit"));
            }
            checkHeightHit = true;
            if (animation.isParameterAvailable("check_height_hit")) {
                checkHeightHit = Boolean.parseBoolean(
                        animation.getParameter("check_height_hit"));
            }
            destroyIfHit = false;
            if (animation.isParameterAvailable("destroy_if_hit")) {
                destroyIfHit = Boolean.parseBoolean(
                                    animation.getParameter("destroy_if_hit"));
            }
            keepOnFloor = false;
            if (animation.isParameterAvailable("keep_on_floor")) {
                keepOnFloor = Boolean.parseBoolean(
                                animation.getParameter("keep_on_floor"));
            }
            affectedByGravity = false;
            if (animation.isParameterAvailable("affected_by_gravity")) {
                affectedByGravity = Boolean.parseBoolean(
                                animation.getParameter("affected_by_gravity"));
            }
            // play associated sound
            if (animation.isParameterAvailable("sound")) {
                Audio.playSound(animation.getParameter("sound"));
            }
        }

        @Override
        public void fixedUpdate() {
            wx += projectileVx;
            wy += projectileVy;
            if (affectedByGravity) {
                projectileVy += GRAVITY;
            }
            if (projectileVx != 0.0) {
                direction = projectileVx > 0 ? RIGHT : LEFT;
            }
            if (tryToFollowPlayerZ) {
                followPlayerZ();
            }
            else {
                wz += projectileVz;
            }
            if (keepOnFloor) {
                wy = Terrain.getHeight(wx, wz) - (checkHeightHit ? 1.0 : 0.0);
            }
            if (!hit && ((checkWalkableHit && !Terrain.isWalkable(wx, wz))
                    || (checkHeightHit && wy >= Terrain.getHeight(wx, wz)))) {
                
                hit();
            }
            animationPlayer.update();
            Animation animation = animationPlayer.getCurrentAnimation();
            double frameIndex = animation.getCurrentFrameIndex();
            int framesCount = animation.getFramesCount();
            if (!animation.isLooping()) {
                if (frameIndex >= framesCount - 0.01) {
                    stateManager.switchTo("none");
                    destroy();
                }
            }
            else {
                destroyIfOutOfViewLeft();
                destroyIfOutOfViewRight();
            }
            tryToSpawnTrail();
        }

        private void followPlayerZ() {
            double targetZ = wz;
            if (projectileVx > 0) {
                double currentWx = Double.MAX_VALUE;
                if (GoldenAxeGame.player1 != null 
                        && GoldenAxeGame.player1.isHittable()) {
                    
                    if (GoldenAxeGame.player1.getWx() > wx) {
                        targetZ = GoldenAxeGame.player1.getWz();
                        currentWx = GoldenAxeGame.player1.getWx();
                    }
                }
                if (GoldenAxeGame.player2 != null 
                        && GoldenAxeGame.player2.isHittable()) {
                    
                    if (GoldenAxeGame.player2.getWx() > wx 
                            && GoldenAxeGame.player2.getWx() < currentWx) {
                        
                        targetZ = GoldenAxeGame.player2.getWz();
                    }
                }
            }
            else if (projectileVx < 0) {
                double currentWx = -Double.MAX_VALUE;
                if (GoldenAxeGame.player1 != null 
                        && GoldenAxeGame.player1.isHittable()) {
                    
                    if (GoldenAxeGame.player1.getWx() < wx) {
                        targetZ = GoldenAxeGame.player1.getWz();
                        currentWx = GoldenAxeGame.player1.getWx();
                    }
                }
                if (GoldenAxeGame.player2 != null 
                        && GoldenAxeGame.player2.isHittable()) {
                    
                    if (GoldenAxeGame.player2.getWx() < wx 
                            && GoldenAxeGame.player2.getWx() > currentWx) {
                        
                        targetZ = GoldenAxeGame.player2.getWz();
                    }
                }
            }
            double difZ = targetZ - wz;
            wz += Math.signum(difZ) * 1.0;
        }
        
    }
    
    private void tryToSpawnTrail() {
        if (!hit && trails != null && Util.getTime() >= nextTrailTime) {
            for (Decoration trail : trails) {
                if (trail.isDestroyed()) {
                    trail.setDestroyed(false);
                    double trx = Util.random(trailRandomX) - trailRandomOffsetX;
                    double trz = Util.random(trailRandomZ) - trailRandomOffsetZ;
                    trail.setWx(wx + trx);
                    trail.setWy(wy);
                    trail.setWz(wz - 0.01 + trz);
                    trail.setVx(0);
                    trail.setVy(0);
                    trail.setVz(0);
                    trail.getStateManager().switchTo("animating");
                    stage.addActor(trail);
                    if (trailSoundId != null) {
                        Audio.playSound(trailSoundId);
                    }
                    nextTrailTime = Util.getTime() + trailInterval;
                    return;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        stateManager.switchTo("none");
    }
    
}
