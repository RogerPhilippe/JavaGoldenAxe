package infra;

import static infra.Settings.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;

/**
 * Audio class.
 * 
 * @author Leonardo Ono (ono.leo80@gmail.com)
 */
public class Audio { 
    
    private static Synthesizer musicSynth;
    private static boolean musicInitialized;
    private static Sequencer sequencer;
    private static int musicVolume;
    private static Music currentMusic;
    
    private static boolean soundInitialized;
    private static Synthesizer synth;
    private static int soundVolume;

    //id midi_file start_tick_position loop_start_tick loop_end_tick        
    public static class Music {
        public final String id;
        public final Sequence sequence;
        public final long startTickPosition;
        public final long loopStartTick;
        public final long loopEndTick;
        public final int loopCount;
        public int cameraX;
        
        public Music(String serializedData) {
            String[] args = serializedData.trim().split(",");
            String[] h = args[0].trim().split("\\s+");
            id = h[1].trim();
            sequence = Resource.loadMusicSequence(args[1].trim());
            startTickPosition = Long.parseLong(args[2].trim());
            loopStartTick = Long.parseLong(args[3].trim());
            loopEndTick = Long.parseLong(args[4].trim());
            loopCount = Integer.parseInt(args[5].trim());
        }        
    }
    
    public static void start() {
        Resource.loadMusics("musics");
        try {
            musicSynth = MidiSystem.getSynthesizer();
            musicSynth.open();
            //Soundbank msb = Resource.loadSoundBank(RES_SOUND_BANK);
            //musicSynth.loadAllInstruments(msb);
            setMusicVolume(2);
             
            sequencer = MidiSystem.getSequencer(false);
            sequencer.open();
            sequencer.getTransmitter().setReceiver(musicSynth.getReceiver());
                    
            musicInitialized = true;
        } catch (MidiUnavailableException ex) {
            Logger.getLogger(Audio.class.getName()).log(Level.SEVERE, null, ex);
            musicInitialized = false;
        }
        
        try {
            synth = MidiSystem.getSynthesizer();
            synth.open();
            Soundbank sb = Resource.loadSoundBank(RES_SOUND_EFFECTS);
            synth.loadAllInstruments(sb);
            soundInitialized = true;
            setSoundVolume(8);
        } catch (Exception ex) {
            Logger.getLogger(Audio.class.getName()).log(Level.SEVERE, null, ex);
            soundInitialized = false;
        }
    }

    public static int getSoundVolume() {
        return soundVolume;
    }
    
    // volume 0~9
    public static void setSoundVolume(int volume) {
        MidiChannel[] channels = synth.getChannels();
        for (int i = 0; i < channels.length; i++) {
            channels[i].controlChange(7, (int) (127 * (volume / 9.0)));
            channels[i].controlChange(39, (int) (127 * (volume / 9.0)));
        }  
        soundVolume = volume;
    }
    
    public static void playSound(String soundName) {
        if (soundInitialized) {
            int soundId = Resource.getSoundId(soundName);
            synth.getChannels()[1].noteOff(soundId, 200);
            synth.getChannels()[1].noteOn(soundId, 200);
        }
    }

    public static void playSound(int soundId) {
        if (soundInitialized) {
            synth.getChannels()[1].noteOff(soundId, 200);
            synth.getChannels()[1].noteOn(soundId, 200);
        }
    }

    public static Music getCurrentMusic() {
        return currentMusic;
    }

    public static int getMusicVolume() {
        return musicVolume;
    }
    
    // volume 0~9
    public static void setMusicVolume(int volume) {
        MidiChannel[] channels = musicSynth.getChannels();
        for (int i = 0; i < channels.length; i++) {
            channels[i].controlChange(7, (int) (127 * (volume / 9.0)));
            channels[i].controlChange(39, (int) (127 * (volume / 9.0)));
        }  
        musicVolume = volume;
    }
    
    public static void playMusic(String musicId) {
        try {
            if (musicInitialized) {
                Music music = Resource.getMusic(musicId);
                Audio.currentMusic = music;
                sequencer.stop();
                sequencer.setSequence(music.sequence);
                //System.out.println("tick length: " 
                //                              + sequencer.getTickLength());
                sequencer.setLoopStartPoint(0);
                sequencer.setLoopEndPoint(0);
                sequencer.setTickPosition(0);
                sequencer.setLoopEndPoint(music.loopEndTick);
                sequencer.setLoopStartPoint(music.loopStartTick);
                sequencer.setTickPosition(music.startTickPosition);
                sequencer.setLoopCount(music.loopCount);
                sequencer.start();
            }
        } 
        catch (InvalidMidiDataException ex) {
            Logger.getLogger(Audio.class.getName()).log(Level.SEVERE, null, ex);
            musicInitialized = false;
        }
    }

    public static void pauseMusic() {
        if (musicInitialized) {
            sequencer.stop();
        }
    }

    public static void resumeMusic() {
        if (musicInitialized) {
            sequencer.start();
        }
    }
    
    public static void stopMusic() {
        if (musicInitialized) {
            sequencer.stop();
        }
        currentMusic = null;
    }

    private static String savedCurrentMusicId;
    
    public static void saveCurrentMusic() {
        savedCurrentMusicId = currentMusic.id;
    }
    
    public static void restoreAndPlaySavedMusic() {
        playMusic(savedCurrentMusicId);
    }
    
    // --- play next music with fade in/out effect ---
    
    private static String nextMusicId;
    private static double nextMusicProgress;
    private static double nextMusicProgressSpeed;
    private static long nextMusicDelayTime;
    private static long nextMusicTime = -1;
    private static int nextMusicOriginalVolume;
    
    public static void playNextMusic(
                    String musicId, double speed, long delayTime) {
        
        nextMusicId = musicId;
        nextMusicProgress = 0.0;
        nextMusicOriginalVolume = musicVolume;
        nextMusicProgressSpeed = speed * (1.0 / musicVolume);
        nextMusicTime = -1;
        nextMusicDelayTime = delayTime;
    }

    public static void update() {
        if (nextMusicId != null && nextMusicTime >= 0 
                            && Util.getTime() >= nextMusicTime) {
            
            if (nextMusicId.equals("empty")) {
                stopMusic();
            }
            else {
                playMusic(nextMusicId);
            }
            setMusicVolume(nextMusicOriginalVolume);
            nextMusicId = null;
        }
        else if (nextMusicId != null && nextMusicProgress < 1.0) {
            nextMusicProgress += nextMusicProgressSpeed;
            if (nextMusicProgress >= 1.0) {
                nextMusicProgress = 1.0;
                nextMusicTime = nextMusicDelayTime + Util.getTime();
            }
            else {
                setMusicVolume((int) ((1.0 - nextMusicProgress) 
                                            * nextMusicOriginalVolume));
            }
        }
    }
    
    public static void main(String[] args) {
        Audio.start();
        Audio.playMusic("ranking");
        Audio.setMusicVolume(3);
    }
    
}