package infra;

import infra.Animation.Frame;
import infra.Animation.FramePoint;
import infra.Audio.Music;
import static infra.Collider.Type.*;
import static infra.Settings.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import static javax.sound.midi.ShortMessage.CONTROL_CHANGE;
import static javax.sound.midi.ShortMessage.MIDI_TIME_CODE;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Track;
import scene.Stage;

/**
 * Resource class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Resource {
    
    private static final Map<String, BufferedImage> IMAGES = new HashMap<>();
    private static final Map<String, AnimationPlayer> ANIMATION_PLAYERS 
                                                        = new HashMap<>();
    
    private static final Map<String, Map<String, String>> ANIMATION_PARAMETERS 
                                                        = new HashMap<>();

    private static final Map<String, List<Collider>> CHAR_COLLIDERS 
                                                        = new HashMap<>();
    
    private static final Map<String, List<FramePoint>> CHAR_POINTS 
                                                        = new HashMap<>();
    
    private static final Map<String, Font> FONTS = new HashMap<>();
    private static final Map<String, Music> MUSICS = new HashMap<>();

    private static final Map<String, Sprite> REUSABLE_STAGE_SPRITES = 
                                                                new HashMap<>();
    
    private static final Map<String, Frame> REUSABLE_STAGE_FRAMES = 
                                                                new HashMap<>();

    private static Properties texts;
    private static Properties soundIds;

    static {
        loadSubimages();
    }
    
    private Resource() {
    }

    public static void clear() {
        REUSABLE_STAGE_SPRITES.clear();
        REUSABLE_STAGE_FRAMES.clear();
        System.gc();
    }
    
    public static BufferedImage getImage(String name) {
        BufferedImage image = null;
        try (
            InputStream is = Resource.class.getResourceAsStream(
                RES_IMAGE_PATH + name + RES_IMAGE_FILE_EXT)) {
            
                image = IMAGES.get(name);
                if (image == null) {
                    image = ImageIO.read(is);
                    IMAGES.put(name, image);
                }
        } catch (Exception ex) {
            Logger.getLogger(
                    Resource.class.getName()).log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
        return image;
    }

    public static Font getFont(String name) {
        Font font = FONTS.get(name);
        if (font == null) {
            String fontResource 
                    = RES_FONT_PATH + name + RES_FONT_FILE_EXT;

            try {
                font = Font.createFont(Font.TRUETYPE_FONT
                    , Resource.class.getResourceAsStream(fontResource));

                FONTS.put(name, font);
            } catch (Exception ex) {
                Logger.getLogger(
                        Resource.class.getName()).log(Level.SEVERE, null, ex);

                System.exit(-1);
            }
        }
        return font;
    }
    
    public static AnimationPlayer getAnimationPlayer(
                                        String res, String subtype) {
        
        //AnimationPlayer animationPlayer =  ANIMATION_PLAYERS.get(res);
        //if (animationPlayer != null) {
        //    return animationPlayer;
        //}
        List<Collider> characterColliders = getCharColliders(res);
        List<FramePoint> characterPoints = getCharPoints(res);
        Map<String, String> parameters = getAnimationParameters(res);
        Map<String, Animation> animations = new HashMap<>();
        String spriteSheetId;
        if (parameters.get(subtype + "_sprite_sheet") != null) {
            spriteSheetId = parameters.get(subtype + "_sprite_sheet");
        }
        else {
            spriteSheetId = res;
        }
        
        try (
            InputStream is = Resource.class.getResourceAsStream(
                        RES_CHARS_PATH + res + RES_ANIMATION_FILE_EXT);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                Animation animation;
                //boolean flipSprite = false;
                boolean useAdditiveBlend = false;
                boolean shadowVisible = true;
                boolean createBlink = true;
                Color blinkColor1 = Color.RED;
                Color blinkColor2 = Color.WHITE;
                double blinkTimeFactor = 0.00000005;
                String line;
                while ((line = br.readLine()) != null) {
                    // System.out.println(line);
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] data = line.split("\\ ");
                    switch (data[0]) {
                        case "sprite_sheet":
                            spriteSheetId = data[1];
                            break;
                        case "blink":
                            createBlink = Boolean.parseBoolean(data[1]);
                            break;
                        case "additive_blend":
                            useAdditiveBlend = Boolean.parseBoolean(data[1]);
                            break;
                        case "shadow":
                            shadowVisible = Boolean.parseBoolean(data[1]);
                            break;
                        case "blink_time_factor":
                            blinkTimeFactor = Double.parseDouble(data[1]);
                            break;
                        case "blink_color_1":
                            blinkColor1 = Util.getColor(data[1]);
                            break;
                        case "blink_color_2":
                            blinkColor2 = Util.getColor(data[1]);
                            break;
                        case "parameter":
                            animation = animations.get(data[1]);
                            if (animation == null) {
                                animation = 
                                        new Animation(data[1], shadowVisible);
                                
                                animation.setBlinkTimeFactor(blinkTimeFactor);
                                animations.put(data[1], animation);
                            }
                            String key = data[2];
                            String value = data[3];
                            animation.setParameter(key, value);
                            break;
                        case "sprite":
                            animation = animations.get(data[1]);
                            if (animation == null) {
                                animation = 
                                        new Animation(data[1], shadowVisible);
                                
                                animation.setBlinkTimeFactor(blinkTimeFactor);
                                animations.put(data[1], animation);
                            }
                            // example: sprite idle RIGHT 173 16 51 55 29 54
                            Direction originalDirection 
                                    = Direction.valueOf(data[2]);
                            
                            int x1 = Integer.parseInt(data[3]);
                            int y1 = Integer.parseInt(data[4]);
                            int w1 = Integer.parseInt(data[5]);
                            int h1 = Integer.parseInt(data[6]);
                            int cx = Integer.parseInt(data[7]);
                            int cy = Integer.parseInt(data[8]);
                            
                            Sprite sprite = getSprite(data[1], spriteSheetId
                                    , x1, y1, w1, h1, cx, cy, originalDirection
                                        , useAdditiveBlend);
                            
                            Rectangle characterRectangle
                                    = new Rectangle(x1, y1, w1, h1);
                            
                            animation.addFrame(sprite, characterRectangle
                                , characterColliders, characterPoints
                                    , shadowVisible, createBlink
                                        , blinkColor1, blinkColor2);
                            
                            break;
                        default:
                            break;
                    }
                }
        }
        catch (Exception ex) {
            Logger.getLogger(Resource.class.getName())
                                    .log(Level.SEVERE, null, ex);
            
            System.exit(-1);
        }        
        AnimationPlayer animationPlayer = 
                new AnimationPlayer(parameters, animations);
        
        //ANIMATION_PLAYERS.put(res, animationPlayer);
        return animationPlayer;
    }

    private static Sprite getSprite(String id, String spriteSheetId
        , int x1, int y1, int w1, int h1, int cx, int cy
            , Direction originalDirection, boolean useAdditiveblend) {
        
        String spriteHash = Sprite.createHash(id, spriteSheetId, x1, y1, w1, h1
                                , cx, cy, originalDirection, useAdditiveblend);        
        
        Sprite sprite = REUSABLE_STAGE_SPRITES.get(spriteHash);
        if (sprite == null) {
            BufferedImage spriteSheet = getImage(spriteSheetId);
            BufferedImage image = spriteSheet.getSubimage(x1, y1, w1, h1);
            sprite = new Sprite(id, image, cx, cy, originalDirection
                                , useAdditiveblend, spriteHash);
            
            REUSABLE_STAGE_SPRITES.put(spriteHash, sprite);
        }
//        else {
//            System.out.println("reusing sprite " + id + " ...");
//        }
        return sprite;
    }

    public static Frame getFrame(Sprite sprite, boolean createShadow
            , boolean createBlink, Color blinkColor1, Color blinkColor2) {
        
        String frameHash = Frame.createHash(
                sprite, createShadow, createBlink, blinkColor1, blinkColor2);
        
        Frame frame = REUSABLE_STAGE_FRAMES.get(frameHash);
        if (frame == null) {
            frame = new Frame(sprite, createShadow
                    , createBlink, blinkColor1, blinkColor2);
            
            REUSABLE_STAGE_FRAMES.put(frameHash, frame);
        }
//        else {
//            System.out.println("reusing frame " + frameHash + " ...");
//        }
        return frame;
    }
    
    private static Map<String, String> getAnimationParameters(String res) {
        Map<String, String> parameters = ANIMATION_PARAMETERS.get(res);
        if (parameters != null) {
            return parameters;
        }
        else {
            parameters = new HashMap<>();
            ANIMATION_PARAMETERS.put(res, parameters);
        }
        try (
            InputStream is = Resource.class.getResourceAsStream(
                    RES_CHARS_PATH + res + RES_ANIMATION_PARAMETERS_FILE_EXT);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            
                String line;
                while ((line = br.readLine()) != null) {
                    //System.out.println(line);
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] data = line.split("\\ ");
                    if (data[0].equals("animation_parameter")) {
                        parameters.put(data[1], data[2]);
                    }
                }
        }
        catch (Exception ex) {
            Logger.getLogger(Resource.class.getName())
                                    .log(Level.SEVERE, null, ex);
            
            System.exit(-1);
        }        
        return parameters;
    }

    private static List<Collider> getCharColliders(String res) {
        List<Collider> colliders = CHAR_COLLIDERS.get(res);
        if (colliders != null) {
            return colliders;
        }
        else {
            colliders = new ArrayList<>();
            CHAR_COLLIDERS.put(res, colliders);
        }
        try (
            InputStream is = Resource.class.getResourceAsStream(
                            RES_CHARS_PATH + res + RES_CHAR_COLLIDER_FILE_EXT);
            
                
            BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            
                String line;
                while ((line = br.readLine()) != null) {
                    //System.out.println(line);
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] data = line.split("\\ ");
                    if (data[0].equals("collider")) {
                        Collider.Type type = Collider.Type.valueOf(data[1]);
                        int damage = 0;
                        int score = 0;
                        int indexFix = 0;
                        if (type == ATTACK) {
                            damage = Integer.parseInt(data[2]);
                            score = Integer.parseInt(data[3]);
                            indexFix = 2;
                        }
                        Collider.Reaction reaction 
                                = Collider.Reaction.valueOf(data[2 + indexFix]);
                        
                        int x = Integer.parseInt(data[3 + indexFix]);
                        int y = Integer.parseInt(data[4 + indexFix]);
                        int width = Integer.parseInt(data[5 + indexFix]);
                        int height = Integer.parseInt(data[6 + indexFix]);
                        Collider currentCollider;
                        colliders.add(currentCollider 
                            = new Collider(x, y, width, height, damage, score));

                        currentCollider.setType(type);
                        currentCollider.setReaction(reaction);
                    }
                }
        }
        catch (Exception ex) {
            Logger.getLogger(Resource.class.getName())
                                    .log(Level.SEVERE, null, ex);
            
            System.exit(-1);
        }        
        return colliders;
    }

    private static List<FramePoint> getCharPoints(String res) {
        List<FramePoint> points = CHAR_POINTS.get(res);
        if (points != null) {
            return points;
        }
        else {
            points = new ArrayList<>();
            CHAR_POINTS.put(res, points);
        }
        try (
            InputStream is = Resource.class.getResourceAsStream(
                            RES_CHARS_PATH + res + RES_CHAR_POINTS_FILE_EXT);
                
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr)) {
            
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] data = line.split("\\ ");
                if (data[0].equals("point")) {
                    String name = data[1];
                    int x = Integer.parseInt(data[2]);
                    int y = Integer.parseInt(data[3]);
                    points.add(new FramePoint(name, null, x, y));
                }
            }
        }
        catch (Exception ex) {
            Logger.getLogger(Resource.class.getName())
                                .log(Level.SEVERE, null, ex);
        }        
        return points;
    }

    public static Map<String, Animator> getAnimators(String res) {
        Map<String, Animator> animators = new HashMap<>();
        
        try (
            InputStream is = Resource.class.getResourceAsStream(
                        RES_ANIMATOR_PATH + res + RES_ANIMATOR_FILE_EXT);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            
            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] data = line.split("\\ ");
                switch (data[0]) {

                    //# animator appearance 0.1 false 
                    //           shadowEnabled shadowActorId shadowAnimationId
                    case "animator" -> {
                        String animatorId = data[1];
                        double speed = Double.parseDouble(data[2]);
                        boolean loop = Boolean.parseBoolean(data[3]);
                        boolean shadowEnabled = Boolean.parseBoolean(data[4]);
                        String shadowActorId = data[5];
                        String shadowAnimationId = data[6];
                        Animator animator = animators.get(animatorId);
                        if (animator == null) {
                            animator = new Animator(animatorId, speed, loop
                                    , shadowEnabled, shadowActorId
                                        , shadowAnimationId);
                            
                            animators.put(animatorId, animator);
                        }
                    }
                    //#add_actor animatorId storchinaya storchinaya purple
                    case "add_actor" -> {
                        String animatorId = data[1];
                        String actorId = data[2];
                        String characterId = data[3];
                        String characterSubtype = null;
                        if (!data[4].equals("null")) {
                            characterSubtype = data[4];
                        }
                        Animator animator = animators.get(animatorId);
                        if (animator == null) {
                            throw new RuntimeException("animator '" 
                                    + animatorId + "' must be created first!");
                        }
                        animator.addActor(
                                actorId, characterId, characterSubtype);
                    }
                    //# keyframe animator_id actor_id frame_index 
                    //           animation_id animation_frame wx wy wz 
                    //           scale_x scale_y rotation_angle 
                    //           tween sound_id 
                    case "keyframe" -> {
                        String animatorId = data[1];
                        String actorId = data[2];
                        int frameIndex = Integer.parseInt(data[3]);
                        String animationId = data[4];
                        int animationFrame = Integer.parseInt(data[5]);
                        double wx = Double.parseDouble(data[6]);
                        double wy = Double.parseDouble(data[7]);
                        double wz = Double.parseDouble(data[8]);
                        double scaleX = Double.parseDouble(data[9]);
                        double scaleY = Double.parseDouble(data[10]);
                        double angle = Double.parseDouble(data[11]);
                        Direction direction = null;
                        if (!data[12].equals("null")) {
                            direction = Direction.valueOf(data[12]);
                        }
                        boolean tween = Boolean.parseBoolean(data[13]);
                        String soundId = data[14];
                        if (data[14].equals("null")) {
                            soundId = null;
                        }
                        Animator animator = animators.get(animatorId);
                        if (animator == null) {
                            throw new RuntimeException("animator '" 
                                    + animatorId + "' must be created first!");
                        }
                        animator.addKeyframe(actorId, frameIndex
                                , animationId, animationFrame, wx, wy, wz
                                    , scaleX, scaleY, angle
                                        , direction, tween, soundId);
                    }
                    //# dialog animatorId 0 1000 ending 6 1 25 20 false 
                    //         0xffffff 0x000000
                    case "dialog" -> {
                        String animatorId = data[1];
                        int startIndex = Integer.parseInt(data[2]);
                        int endIndex = Integer.parseInt(data[3]);
                        String textId = data[4];
                        int col = Integer.parseInt(data[5]);
                        int row = Integer.parseInt(data[6]);
                        int width = Integer.parseInt(data[7]);
                        int height = Integer.parseInt(data[8]);
                        boolean baloonVisible = Boolean.parseBoolean(data[9]);
                        Color textColor = Util.getColor(data[10]);
                        Color shadowColor = Util.getColor(data[11]);
                        Animator animator = animators.get(animatorId);
                        if (animator == null) {
                            throw new RuntimeException("animator '" 
                                    + animatorId + "' must be created first!");
                        }
                        animator.addDialogFrame(textId, startIndex, endIndex
                                , col, row, width, height, baloonVisible
                                    , textColor, shadowColor);
                    }
                    //# event title 180 bbb
                    case "event" -> {
                        String animatorId = data[1];
                        int frameIndex = Integer.parseInt(data[2]);
                        String eventId = data[3];
                        Animator animator = animators.get(animatorId);
                        if (animator == null) {
                            throw new RuntimeException("animator '" 
                                    + animatorId + "' must be created first!");
                        }
                        animator.addEvent(frameIndex, eventId, data);
                    }
                    default -> {
                    }
                }
            }
        }
        catch (Exception ex) {
            Logger.getLogger(Resource.class.getName())
                                    .log(Level.SEVERE, null, ex);
            
            System.exit(-1);
        }        
        return animators;
    }
    
    public static String getText(String textId) {
        return getTexts().getProperty(textId);
    }
    
    public static Properties getTexts() {
        if (texts == null) {
            texts = new Properties();
            InputStream is = Resource.class.getResourceAsStream(
                    RES_INF_PATH + RES_TEXTS + RES_TEXTS_FILE_EXT);

            try {
                texts.load(is);
            } catch (IOException ex) {
                Logger.getLogger(
                        Resource.class.getName()).log(Level.SEVERE, null, ex);

                System.exit(-1);
            }
        }
        return texts;
    }
    
    public static int getSoundId(String soundName) {
        return Integer.parseInt(getSoundIds().getProperty(soundName));
    }
    
    public static Properties getSoundIds() {
        if (soundIds == null) {
            soundIds = new Properties();
            InputStream is = Resource.class.getResourceAsStream(
                    RES_INF_PATH + RES_SOUND_IDS + RES_SOUND_IDS_FILE_EXT);

            try {
                soundIds.load(is);
            } catch (IOException ex) {
                Logger.getLogger(
                        Resource.class.getName()).log(Level.SEVERE, null, ex);

                System.exit(-1);
            }
        }
        return soundIds;
    }
    
    public static Soundbank loadSoundBank(String res) {
        String resTmp = RES_SOUND_PATH + res + RES_SOUND_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp)) {
            BufferedInputStream bis = new BufferedInputStream(is);
            return MidiSystem.getSoundbank(bis);
        } 
        catch (IOException | InvalidMidiDataException ex) {
            Logger.getLogger(Resource.class.getName())
                    .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
        return null;
    }
    
    public static Sequence loadMusicSequence(String midiFile) {
        String res = RES_MUSIC_PATH + midiFile + RES_MUSIC_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(res)) {
            Sequence sequence = MidiSystem.getSequence(is);
            
            // remove the volume control messages to allow 
            // to change volume from the program
            List<MidiEvent> controlEventsToRemove = new ArrayList<>();
            for (Track track : sequence.getTracks()) {
                controlEventsToRemove.clear();
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent me = track.get(i);
                    MidiMessage mm = me.getMessage();
                    boolean controlMsg = 
                        (mm.getStatus() & CONTROL_CHANGE) == CONTROL_CHANGE;
                    
                    boolean timeMsg = 
                        (mm.getStatus() & MIDI_TIME_CODE) == MIDI_TIME_CODE;
                    
                    if (controlMsg && !timeMsg) {
                        controlEventsToRemove.add(me);
                    }
                }
                controlEventsToRemove.forEach(me -> track.remove(me));
            }
            
            return sequence;
        } 
        catch (Exception ex) {
            Logger.getLogger(Resource.class.getName())
                    .log(Level.SEVERE, null, ex);
            
            System.exit(-1);
        }
        return null;
    }

    //id midi_file start_tick_position loop_start_tick loop_end_tick        
    public static void loadMusics(String res) {
        MUSICS.clear();
        String resTmp = RES_INF_PATH + res + RES_INF_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            Music music = null;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() 
                        || line.startsWith("#") || line.startsWith(";")) {
                    
                    continue;
                }
                if (line.startsWith("music ")) {
                    music = new Music(line);
                    MUSICS.put(music.id, music);
                }
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(Resource.class.getName())
                                .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
    }
    
    public static Music getMusic(String musicId) {
        return MUSICS.get(musicId);
    }
    
    public static void loadStage(int stage, Stage stageScene) {
        String resTmp = RES_STAGE_PATH + "stage_" + stage + RES_STAGE_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() 
                        || line.startsWith("#") || line.startsWith(";")) {
                    
                    continue;
                }
                String data[] = line.split("\\ ");
                if (line.startsWith("stage ")) {
                    GoldenAxeGame.stageId = data[1];
                }
                else if (line.startsWith("background ")) {
                    // background layer transitionId imageRes scrollVx scroolVy camOffsetX camOffsetY camScaleX camScaleY tiled
                    int layer = Integer.parseInt(data[1]);
                    String transitionId = data[2];
                    String imageRes = data[3];
                    double scrollVx = Double.parseDouble(data[4]);
                    double scrollVy = Double.parseDouble(data[5]);
                    double camOffsetX = Double.parseDouble(data[6]);
                    double camOffsetY = Double.parseDouble(data[7]);
                    double camScaleX = Double.parseDouble(data[8]);
                    double camScaleY = Double.parseDouble(data[9]);
                    boolean tiled = Boolean.parseBoolean(data[10]);
                    Background background = new Background(imageRes
                            , scrollVx, scrollVy, camOffsetX, camOffsetY
                                , camScaleX, camScaleY, tiled);
                            
                    stageScene.addBackground(
                            layer, transitionId, background);
                }
                else if (line.startsWith("terrain_collision_mask ")) {
                    BufferedImage collisionMask = getImage(data[1]);
                    Terrain.setCollisionMask(collisionMask);
                }
                else if (line.startsWith("terrain_dead_height ")) {
                    int deadHeight = Integer.parseInt(data[1]);
                    Terrain.setDeathHeight(deadHeight);
                }
                else if (line.startsWith("camera_position ")) {
                    int cameraX = Integer.parseInt(data[1]);
                    int cameraY = Integer.parseInt(data[2]);
                    Camera.setX(cameraX);
                    Camera.setY(cameraY);
                }
                else if (line.startsWith("camera_max_x ")) {
                    int cameraMaxX = Integer.parseInt(data[1]);
                    Camera.setMaxX(cameraMaxX);
                }
                else if (line.startsWith("camera_min_y ")) {
                    Camera.setMinY(data);
                }
                else if (line.startsWith("camera_max_y ")) {
                    Camera.setMaxY(data);
                }
                else if (line.startsWith("terrain_magic_path_z ")) {
                    Terrain.setMagicPathZ(data);
                }
                else if (line.startsWith("camera_lock ")) {
                    String cameraLockingId = data[1];
                    int x = Integer.parseInt(data[2]);
                    boolean nextStage = Boolean.parseBoolean(data[3]);
                    long nextStageWaitTime = Long.parseLong(data[4]);
                    boolean autoScroll = Boolean.parseBoolean(data[5]);
                    int playersMoveMargin = Integer.parseInt(data[6]);
                    Camera.addLock(cameraLockingId, x, nextStage
                        , nextStageWaitTime, autoScroll, playersMoveMargin);
                }
                else if (line.startsWith("command ")) {
                    Command command = new Command(stageScene, data);
                    stageScene.addActor(command);
                }
                //System.out.println("stage line: " + line);
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(Resource.class.getName())
                                .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
    }
    
    private static void loadSubimages() {
        String resTmp = RES_INF_PATH + RES_SUBIMAGES_INF + RES_INF_FILE_EXT;
        try (InputStream is = Resource.class.getResourceAsStream(resTmp);
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() 
                        || line.startsWith("#") || line.startsWith(";")) {
                    
                    continue;
                }
                String data[] = line.split("\\ ");
                if (line.startsWith("subimage ")) {
                    // subimage stage_1_A stage_full 600 0 600 578 
                    String subimageId = data[1];
                    String imageId = data[2];
                    int x = Integer.parseInt(data[3]);
                    int y = Integer.parseInt(data[4]);
                    int w = Integer.parseInt(data[5]);
                    int h = Integer.parseInt(data[6]);
                    BufferedImage subimage = getImage(imageId)
                                                .getSubimage(x, y, w, h);
                    
                    if (data.length == 8) {
                        BufferedImage paletteImage = getImage(data[7]);
                        subimage = replaceColors(subimage, paletteImage);
                    }
                    IMAGES.put(subimageId, subimage);
                }
            }
        } 
        catch (Exception ex) {
            Logger.getLogger(Resource.class.getName())
                                .log(Level.SEVERE, null, ex);

            System.exit(-1);
        }
    }
    
    private static BufferedImage replaceColors(BufferedImage originalImage
                                , BufferedImage paletteImage) throws Exception {
        
        if (paletteImage.getHeight() != 2) {
            throw new Exception("palette image must have height 2 !");
        }
        Map<Integer, Integer> palette = new HashMap<>();
        // extract the colors from palette image
        for (int px = 0; px < paletteImage.getWidth(); px++) {
            int originalColor = paletteImage.getRGB(px, 0);
            int targetColor = paletteImage.getRGB(px, 1);
            palette.put(originalColor, targetColor);
        }
        // replace colors
        int replacedImageWidth = originalImage.getWidth();
        int replacedImageHeight = originalImage.getHeight();
        BufferedImage replacedImage = new BufferedImage(
                replacedImageWidth, replacedImageHeight
                    , BufferedImage.TYPE_INT_ARGB);
        
        int[] replacedImageData = ((DataBufferInt) replacedImage
                                    .getRaster().getDataBuffer()).getData();
        
        for (int ry = 0; ry < replacedImageHeight; ry++) {
            for (int rx = 0; rx < replacedImageWidth; rx++) {
                int imageColor = originalImage.getRGB(rx, ry);
                Integer replaceColor = palette.get(imageColor);
                if (replaceColor != null) {
                    replacedImageData[rx + ry * replacedImageWidth] 
                                                            = replaceColor;
                }
            }
        }
        return replacedImage;
    }

    private static BufferedImage getSubimage(String data[]) {
        BufferedImage image = Resource.getImage(data[1]);
        int x = Integer.parseInt(data[2]);
        int y = Integer.parseInt(data[3]);
        int w = Integer.parseInt(data[4]);
        int h = Integer.parseInt(data[5]);
        return image.getSubimage(x, y, w, h);
    }
    
}
