package infra;

import java.awt.Color;
import java.awt.event.KeyEvent;
import javax.sound.sampled.AudioFormat;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;

/**
 * (Project) Settings class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Settings {
    
    // --- display ---
    
    public static final int CANVAS_WIDTH = 276;
    public static final int CANVAS_HEIGHT = 207;

    public static final int PREFERRED_SCREEN_WIDTH = (int) (276 * 2.5);
    public static final int PREFERRED_SCREEN_HEIGHT = (int) (207 * 2.5);
    
    public static final double ASPECT_RATIO 
                = PREFERRED_SCREEN_WIDTH / (double) PREFERRED_SCREEN_HEIGHT;
    
    public static boolean keepAspectRatio = true;

    public static final int KEY_KEEP_ASPECT_RATIO = KeyEvent.VK_F11;
    public static final int KEY_FULLSCREEN = KeyEvent.VK_F12;


    // --- game loop ---
    
    public static final long TIMER_PER_FRAME = 1000000000 / 60;

    
    // --- resources ---
    
    public static final AudioFormat SOUND_AUDIO_FORMAT 
            = new AudioFormat(PCM_UNSIGNED, 11025, 8, 1, 1, 11025, true);
    
    public static final String RES_INF_FILE_EXT = ".inf"; 
    public static final String RES_IMAGE_FILE_EXT = ".png";
    public static final String RES_MUSIC_FILE_EXT = ".mid"; 
    public static final String RES_SOUND_FILE_EXT = ".sf2"; 
    public static final String RES_FONT_FILE_EXT = ".ttf"; 
    public static final String RES_STAGE_FILE_EXT = ".inf"; 
    public static final String RES_SOUND_IDS_FILE_EXT = ".inf"; 
    public static final String RES_TEXTS_FILE_EXT = ".inf"; 
    public static final String RES_ANIMATION_FILE_EXT = ".char";
    public static final String RES_ANIMATION_PARAMETERS_FILE_EXT = ".char";
    public static final String RES_ANIMATOR_FILE_EXT = ".ani";
    public static final String RES_CHAR_COLLIDER_FILE_EXT = ".char";
    public static final String RES_CHAR_POINTS_FILE_EXT = ".char";
    
    public static final String RES_INF_PATH = "/res/inf/";
    public static final String RES_IMAGE_PATH = "/res/image/";
    public static final String RES_CHARS_PATH = "/res/chars/";
    public static final String RES_ANIMATOR_PATH = "/res/anim/";
    public static final String RES_MUSIC_PATH = "/res/audio/";
    public static final String RES_SOUND_PATH = "/res/audio/";
    public static final String RES_FONT_PATH = "/res/font/";
    public static final String RES_STAGE_PATH = "/res/stage/";
    
    public static final String RES_DEFAULT_FONT = "golden_axe";
    public static final String RES_SOUND_BANK = "tinypsg";
    public static final String RES_SOUND_EFFECTS = "sound_effects";
    public static final String RES_SOUND_IDS = "sounds";
    public static final String RES_TEXTS = "texts";
    
    public static final String RES_MUSICS_INF = "musics";
    public static final String RES_SUBIMAGES_INF = "subimages";
    
    // --- physics ---
    
    public static final double GRAVITY = 0.2;
    public static final int VALID_COLLISION_WZ_DIF = 16;

    
    // --- input (changeable) ---
    
    public static int KEY_START_1 = KeyEvent.VK_SPACE;
    public static int KEY_START_2 = KeyEvent.VK_ENTER;
    public static int KEY_CANCEL = KeyEvent.VK_ESCAPE;

    public static int KEY_PLAYER_1_UP = KeyEvent.VK_UP;
    public static int KEY_PLAYER_1_DOWN = KeyEvent.VK_DOWN;
    public static int KEY_PLAYER_1_LEFT = KeyEvent.VK_LEFT;
    public static int KEY_PLAYER_1_RIGHT = KeyEvent.VK_RIGHT;
    public static int KEY_PLAYER_1_MAGIC = KeyEvent.VK_I;
    public static int KEY_PLAYER_1_ATTACK = KeyEvent.VK_O;
    public static int KEY_PLAYER_1_JUMP = KeyEvent.VK_P;

    public static int KEY_PLAYER_2_UP = KeyEvent.VK_W;
    public static int KEY_PLAYER_2_DOWN = KeyEvent.VK_S;
    public static int KEY_PLAYER_2_LEFT = KeyEvent.VK_A;
    public static int KEY_PLAYER_2_RIGHT = KeyEvent.VK_D;
    public static int KEY_PLAYER_2_MAGIC = KeyEvent.VK_F;
    public static int KEY_PLAYER_2_ATTACK = KeyEvent.VK_G;
    public static int KEY_PLAYER_2_JUMP = KeyEvent.VK_H;
    
    // --- life ---
    
    public static int CREDITS = 20;
    public static int LIVES = 3;
    public static int ENERGY_BARS = 5;
    public static final int MAX_ENERGY = 16 * 5;
    
    
    // --- debug ---
    
    public static boolean SHOW_DEBUG_TERRAIN_HEIGHT = false;
    public static boolean SHOW_DEBUG_SPRITE_CENTER = false;
    public static boolean SHOW_DEBUG_SPRITE_BOUNDING_BOX = false;
    public static boolean SHOW_DEBUG_COLLIDERS = false;
    public static boolean SHOW_DEBUG_POINTS = false;
    public static boolean SHOW_DEBUG_CAMERA_INFO = false;
    public static boolean SHOW_DEBUG_PLAYER_1_INFO = false;
    public static boolean SHOW_DEBUG_PLAYER_2_INFO = false;
    public static boolean SHOW_DEBUG_ENEMIES_COUNT = false;
    public static Color DEBUG_SPRITE_CENTER_COLOR = Color.RED;
    public static Color DEBUG_SPRITE_BOUNDING_BOX_COLOR = Color.BLUE;
    public static Color DEBUG_COLLIDERS_BODY_COLOR = new Color(0, 0, 255, 128);
    public static Color DEBUG_COLLIDERS_ATTACK_COLOR 
                                                = new Color(255, 128, 128, 128);

    public static Color DEBUG_COLLIDERS_FIRE_COLOR 
                                                = new Color(255, 255, 0, 128);
    
    public static Color DEBUG_COLLIDERS_TOUCH_COLOR 
                                                = new Color(255, 0, 0, 128);
    
    public static Color DEBUG_POINTS_COLOR = new Color(128, 255, 64, 128);
    
}
