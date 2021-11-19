package infra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Animator class.
 * 
 * Allows multiple actors to be animated in a synchronized way.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Animator {

    // keyframe actor_id frame_index animation_id animation_frame wx wy wz 
    //          scale_x scale_y rotation_angle sound_id
    public static class AnimatorFrame {
        
        public String actorId;
        public double frameIndex;
        public String animationId;
        public double animationFrame;
        public double wx;
        public double wy;
        public double wz;
        public double scaleX;
        public double scaleY;
        public double angle;
        public Direction direction;
        public boolean tween;
        public String soundId;
        public long soundPlayedId;
        
        public AnimatorFrame() {
        }

        public AnimatorFrame(String actorId, double frameIndex, String animationId
                , double animationFrame, double wx, double wy, double wz
                    , double scaleX, double scaleY, double angle
                        , Direction direction, boolean tween, String soundId) {
            
            this.actorId = actorId;
            this.frameIndex = frameIndex;
            this.animationId = animationId;
            this.animationFrame = animationFrame;
            this.wx = wx;
            this.wy = wy;
            this.wz = wz;
            this.scaleX = scaleX;
            this.scaleY = scaleY;
            this.angle = angle;
            this.direction = direction;
            this.tween = tween;
            this.soundId = soundId;
        }

        public void set(AnimatorFrame otherKeyframe) {
            this.actorId = otherKeyframe.actorId;
            this.frameIndex = otherKeyframe.frameIndex;
            this.animationId = otherKeyframe.animationId;
            this.animationFrame = otherKeyframe.animationFrame;
            this.wx = otherKeyframe.wx;
            this.wy = otherKeyframe.wy;
            this.wz = otherKeyframe.wz;
            this.scaleX = otherKeyframe.scaleX;
            this.scaleY = otherKeyframe.scaleY;
            this.angle = otherKeyframe.angle;
            this.direction = otherKeyframe.direction;
            this.tween = otherKeyframe.tween;
            this.soundId = otherKeyframe.soundId;
        }
        
        public static void lerp(double frameIndex, AnimatorFrame previousFrame
                        , AnimatorFrame nextFrame, AnimatorFrame resultFrame) {
            
            double p = (double) (frameIndex - previousFrame.frameIndex) 
                        / (nextFrame.frameIndex - previousFrame.frameIndex);
            
            if (frameIndex < nextFrame.frameIndex) {
                resultFrame.actorId = previousFrame.actorId;
                resultFrame.animationId = previousFrame.animationId;
                resultFrame.animationFrame = previousFrame.animationFrame;
                resultFrame.direction = previousFrame.direction;
            }
            else {
                resultFrame.actorId = nextFrame.actorId;
                resultFrame.animationId = nextFrame.animationId;
                resultFrame.animationFrame = nextFrame.animationFrame;
                resultFrame.direction = nextFrame.direction;
            }

            resultFrame.frameIndex = frameIndex;
            resultFrame.wx = previousFrame.wx 
                    + p * (nextFrame.wx - previousFrame.wx);
            
            resultFrame.wy = previousFrame.wy 
                    + p * (nextFrame.wy - previousFrame.wy);
            
            resultFrame.wz = previousFrame.wz 
                    + p * (nextFrame.wz - previousFrame.wz);
            
            resultFrame.scaleX = previousFrame.scaleX 
                    + p * (nextFrame.scaleX - previousFrame.scaleX);
            
            resultFrame.scaleY = previousFrame.scaleY 
                    + p * (nextFrame.scaleY - previousFrame.scaleY);
            
            resultFrame.angle = previousFrame.angle 
                    + p * (nextFrame.angle - previousFrame.angle);
            
            resultFrame.tween = true;
            
            if (frameIndex == previousFrame.frameIndex) {
                resultFrame.soundId = previousFrame.soundId;
            }
            else if (frameIndex == nextFrame.frameIndex) {
                resultFrame.soundId = nextFrame.soundId;
            }
            else {
                resultFrame.soundId = null;
            }
        }
        
    }
    
    public static class DialogFrame {

        public String textId;
        public int startFrame;
        public int endFrame;
        public int col, row, width, height;
        public boolean baloonVisible;
        public Color textColor;
        public Color shadowColor;
        public boolean started;
        public boolean finished;
        
        public DialogFrame(String textId, int startFrame, int endFrame
            , int col, int row, int width, int height, boolean baloonVisible
                , Color textColor, Color shadowColor) {
            
            this.textId = textId;
            this.startFrame = startFrame;
            this.endFrame = endFrame;
            this.col = col;
            this.row = row;
            this.width = width;
            this.height = height;
            this.baloonVisible = baloonVisible;
            this.textColor = textColor;
            this.shadowColor = shadowColor;
            this.started = false;
            this.finished = false;
        }

        public void reset() {
            this.started = false;
            this.finished = false;
        }
        
    }
    
    public static class AnimatorEvent implements Comparable<AnimatorEvent> {
        
        public int frameIndex;
        public String eventId;
        public String[] data;

        public AnimatorEvent(int frameIndex, String eventId, String[] data) {
            this.frameIndex = frameIndex;
            this.eventId = eventId;
            this.data = data;
        }

        @Override
        public int compareTo(AnimatorEvent o) {
            return frameIndex - o.frameIndex;
        }
        
    }
    
    private final String name;
    
    // actors
    private final List<Actor> actors = new ArrayList<>();
    private final Map<String, Actor> actorsById = new HashMap<>();
    
    // actorId <-> keyframes
    private final Map<String, TreeMap<Integer, AnimatorFrame>> keyframes = 
                                                                new HashMap<>();
    
    private final List<DialogFrame> dialogKeyframes = new ArrayList<>();
    private final List<AnimatorEvent> events = new ArrayList<>();
            
    private double frameIndex;
    private double speed;
    private boolean playing;
    private boolean loop;
    private boolean finished;
    private int lastSoundPlayedId;
    
    private int eventIndex;
    private AnimatorEventListener listener;
    
    private boolean shadowEnabled;
    private String shadowActorId;
    private String shadowAnimationId;
    
    public Animator(String name, double speed, boolean loop
        , boolean shadowEnabled, String shadowActorId
            , String shadowAnimationId) {
        
        this.name = name;
        this.speed = speed;
        this.loop = loop;
        
        this.shadowEnabled = shadowEnabled;
        this.shadowActorId = shadowActorId;
        this.shadowAnimationId = shadowAnimationId;
    }

    public String getName() {
        return name;
    }
    
    public void addActor(
            String actorId, String charactererId, String characterSubtype) {
        
        Actor actor = new Actor(
                null, actorId, 0, 0, charactererId, characterSubtype, null);
    
        addActor(actor);
    }
    
    public void addActor(Actor actor) {
        actors.add(actor);
        actorsById.put(actor.getId(), actor);
    }

    public List<Actor> getActors() {
        return actors;
    }

    public Map<String, Actor> getActorsById() {
        return actorsById;
    }
    
    public void addKeyframe(String actorId, int frameIndex
            , String animationId, int animationFrame
                , double wx, double wy, double wz
                    , double scaleX, double scaleY, double angle
                        , Direction direction, boolean tween, String soundId) {
        
        TreeMap<Integer, AnimatorFrame> actorKeyframes = keyframes.get(actorId);
        if (actorKeyframes == null) {
            actorKeyframes = new TreeMap<>();
            keyframes.put(actorId, actorKeyframes);
        }
        
        AnimatorFrame actorKeyframe = new AnimatorFrame(actorId, frameIndex
                , animationId, animationFrame, wx, wy, wz
                    , scaleX, scaleY, angle, direction, tween, soundId);
        
        actorKeyframes.put(frameIndex, actorKeyframe);
    }
    
    public void addDialogFrame(String textId, int startFrame, int endFrame
            , int col, int row, int width, int height, boolean baloonVisible
                , Color textColor, Color shadowColor) {
        
        dialogKeyframes.add(new DialogFrame(textId, startFrame, endFrame
            , col, row, width, height, baloonVisible, textColor, shadowColor));
    }
    
    public void addEvent(int frameIndex, String eventId, String[] data) {
        events.add(new AnimatorEvent(frameIndex, eventId, data));
    }
    
    public double getFrameIndex() {
        return frameIndex;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public AnimatorEventListener getListener() {
        return listener;
    }

    public void setListener(AnimatorEventListener listener) {
        this.listener = listener;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isFinished() {
        return finished;
    }
    
    public void play() {
        frameIndex = 0.0;
        playing = true;
        finished = false;
        lastSoundPlayedId++;
        eventIndex = 0;
        dialogKeyframes.forEach(frame -> frame.reset());
        Collections.sort(events);
    }

    public void pause() {
        playing = false;
    }
    
    public void resume() {
        playing = true;
    }
    
    private final AnimatorFrame resultFrame = new AnimatorFrame();
    
    public void fixedUpdate() {
        fixedUpdate(actorsById);
    }
    
    public void fixedUpdate(Map<String, Actor> actors) {
        update(actors, 1.0);
    }

    public void update(double lerp) {
        update(actorsById, lerp);
    }
    
    public void update(Map<String, Actor> actors, double lerp) {
        if (!playing || isFinished()) {
            return;
        }
        
        boolean allActorsFinished = true;
        
        frameIndex += speed * lerp;
        int frameIndexInt = (int) frameIndex;
        for (String actorId : keyframes.keySet()) {
            
            Actor actor = actors.get(actorId);
            if (actor == null) {
                //throw new RuntimeException("Animator " 
                //        + name + " couldn't find actor '" + actorId + "' !");
                continue;
            }
            
            TreeMap<Integer, AnimatorFrame> actorKeyframes = 
                                                        keyframes.get(actorId);
            
            Integer floorIndex = actorKeyframes.floorKey(frameIndexInt);
            AnimatorFrame floorFrame = actorKeyframes.get(floorIndex);
            Integer ceilIndex = actorKeyframes.ceilingKey(floorIndex + 1);

            // play sound
            if (floorFrame.soundId != null 
                    && floorFrame.soundPlayedId != lastSoundPlayedId) {
                
                Audio.playSound(floorFrame.soundId);
                floorFrame.soundPlayedId = lastSoundPlayedId;
            }
            
            if (floorFrame.tween) {
                if (ceilIndex != null) {
                    AnimatorFrame ceilFrame = actorKeyframes.get(ceilIndex);
                    AnimatorFrame.lerp(
                            frameIndex, floorFrame, ceilFrame, resultFrame);
                }
                else {
                    resultFrame.set(floorFrame);
                }
            }
            else {
                resultFrame.set(floorFrame);
            }
            
            actor.playAnimatorFrame(resultFrame);
            actor.fixedUpdate();
            
            if (ceilIndex != null) {
                allActorsFinished = false;
            }
            else {
                // actor animator finished TODO
            }
        }
        
        if (allActorsFinished && loop) {
            frameIndex = 0.0;
            allActorsFinished = false;
        }
        
        boolean dialogFinished = true;
        
        // dialog frames
        for (DialogFrame dialogFrame : dialogKeyframes) {
            if (!dialogFrame.finished 
                    && frameIndexInt >= dialogFrame.startFrame 
                        && frameIndexInt <= dialogFrame.endFrame) {
                
                dialogFinished = false;
                
                if (!dialogFrame.started) {
                    dialogFrame.started = true;
                    String text = Resource.getText(dialogFrame.textId);
                    if (text.equals("_hide_")) {
                        dialogFrame.finished = true;
                        Dialog.hide();
                    }
                    else {
                        Dialog.show(dialogFrame.col, dialogFrame.row
                                , dialogFrame.width, dialogFrame.height
                                    , text, dialogFrame.baloonVisible
                                        , dialogFrame.textColor
                                        , dialogFrame.shadowColor);
                    }
                }
                
                Dialog.updateAnimator(frameIndexInt
                        , dialogFrame.startFrame, dialogFrame.endFrame);

                if (frameIndexInt >= dialogFrame.endFrame 
                                            && !dialogFrame.finished) {
                    
                    dialogFrame.finished = true;
                }
                
                break;
            }
        }
        
        boolean eventsFinished = eventIndex > events.size() - 1;
        
        if (!events.isEmpty()) {
            AnimatorEvent event = null;
            while (eventIndex <= events.size() - 1) {
                event = events.get(eventIndex);
                if (frameIndexInt >= event.frameIndex) {
                    eventIndex++;
                    if (listener != null) {
                        listener.onAnimatorEventTriggered(event);
                    }
                }
                else {
                    break;
                }
            }
        }
        
        if (allActorsFinished && dialogFinished && eventsFinished) {
            finished = true;
        }
        if (playing) {
            playing = !finished;
        }
    }
    
    public void drawShadow(Offscreen shadowLayer
            , int cameraX, int cameraY, double shadowShear) {
        
        drawShadow(actors, shadowLayer, cameraX, cameraY, shadowShear);
    }
    
    public void drawShadow(List<Actor> actorsTmp, Offscreen shadowLayer
            , int cameraX, int cameraY, double shadowShear) {
        
        actorsTmp.forEach(actor 
            -> actor.drawShadow(shadowLayer, cameraX, cameraY, shadowShear));
    }

    public void draw(Graphics2D g
            , Offscreen shadowLayer, double cameraX, double cameraY) {
        
        draw(actors, g, shadowLayer, cameraX, cameraY);
    }
    
    public void draw(List<Actor> actorsTmp, Graphics2D g
            , Offscreen shadowLayer, double cameraX, double cameraY) {
        
        Collections.sort(actorsTmp);
        actorsTmp.forEach(actor -> {
            actor.draw(g, 0, 0);
            Animation animation = 
                    actor.getAnimationPlayer().getCurrentAnimation();

            if (animation != null
                    && shadowEnabled && actor.getId().equals(shadowActorId) 
                        && animation.getName().equals(shadowAnimationId)) {

                g.drawImage(shadowLayer.getImage(), 0, 0, null);
            }
        });
        Dialog.draw(g);
    }
    
}
