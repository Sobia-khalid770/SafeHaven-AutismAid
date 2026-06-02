package safehaven.utils;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import safehaven.utils.ThreadPoolManager;
import safehaven.utils.Logger;

/**
 * Plays animal / feedback sounds.
 * Supports both file-based playback (.wav files from sounds/ folder)
 * and programmatically generated tones as fallback.
 */
public class SoundManager {
    private static SoundManager instance;
    private float volume = 0.8f;
    
    // Base path for sound files - resolved relative to project root
    private String soundsDir;

    private SoundManager() {
        // Try to find the sounds directory
        soundsDir = findSoundsDir();
        if (soundsDir != null) {
            Logger.info("Sounds directory found: " + soundsDir);
        } else {
            Logger.warn("Sounds directory not found; using synthesized sounds as fallback");
        }
    }

    public static synchronized SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    //  Feedback sounds (synthesized) 

    public void playCorrect()   { playTone(880, 150, 0.6f); delay(100); playTone(1100, 200, 0.5f); }
    public void playWrong()     { playTone(300, 200, 0.4f); delay(80);  playTone(250, 300, 0.3f); }
    public void playCheer()     {
        // Try file first, then fallback to synthesized
        if (playFileIfExists("cheers.wav")) return;
        int[] notes = {523, 659, 784, 1047};
        for (int n : notes) { playTone(n, 120, 0.5f); delay(80); }
    }
    public void playBubblePop() { playTone(700 + (int)(Math.random()*400), 80, 0.35f); }
    public void playClick()     { playTone(600, 50, 0.2f); }
    public void playWow()       {
        int[] notes = {523, 784, 1047, 1319};
        for (int n : notes) { playTone(n, 100, 0.45f); delay(60); }
    }

    //  Animal / object sounds (file-based with synthesized fallback) 

    public void playCat() {
        if (playFileIfExists("cat sound.wav")) return;
        // Fallback: synthesized meow
        playTone(900, 200, 0.7f); delay(100);
        playTone(800, 150, 0.6f); delay(80);
        playTone(700, 100, 0.5f);
    }

    public void playDog() {
        if (playFileIfExists("dog sound.wav")) return;
        // Fallback: synthesized bark
        playTone(250, 200, 0.8f); delay(120);
        playTone(280, 200, 0.7f);
    }

    public void playCow() {
        if (playFileIfExists("cow sound.wav")) return;
        // Fallback: synthesized moo
        playTone(100, 600, 0.8f);
    }

    public void playBird() {
        // birds sound.mp3 - MP3 not supported natively, try anyway, then fallback
        if (playFileIfExists("birds sound.mp3")) return;
        // Fallback: synthesized chirps
        playTone(2000, 100, 0.6f); delay(80);
        playTone(2200, 100, 0.6f); delay(80);
        playTone(2400, 100, 0.6f); delay(80);
        playTone(2600, 100, 0.6f);
    }

    public void playBell() {
        if (playFileIfExists("bell.wav")) return;
        // Fallback
        playCorrect();
    }

    public void playHorn() {
        if (playFileIfExists("horn sound.wav")) return;
        // Fallback
        playWow();
    }

    public void playTabla() {
        // tabla sound.mp3 - MP3 not supported natively, try anyway, then fallback
        if (playFileIfExists("tabla sound.mp3")) return;
        // Fallback: synthesized drum pattern
        for (int i = 0; i < 4; i++) {
            playBubblePop();
            delay(150);
        }
    }

    public void playCheers() {
        if (playFileIfExists("cheers.wav")) return;
        // Fallback
        int[] notes = {523, 659, 784, 1047};
        for (int n : notes) { playTone(n, 120, 0.5f); delay(80); }
    }

    //  Number and letter sounds (kept for backward compatibility) 
    
    public void playNumber(int num) {
        if (num < 1 || num > 20) return;
        int[] numberFreqs = {
            262, 294, 330, 349, 392, 440, 494, 523, 587, 659,
            698, 784, 880, 988, 1047, 1175, 1319, 1397, 1568, 1760
        };
        int freq = numberFreqs[num - 1];
        playTone(freq, 400, 0.9f);
        if (num == 20) { delay(300); playCheer(); }
    }
    
    public void playLetter(String letter, String word) {
        if (letter == null || letter.isEmpty() || word == null) {
            Logger.warn("playLetter called with null/empty parameters");
            return;
        }
        int letterIndex = letter.toUpperCase().charAt(0) - 'A';
        if (letterIndex < 0 || letterIndex >= 26) { playCorrect(); return; }
        int[] letterFreqs = {
            262, 294, 330, 349, 392, 440, 494, 523, 587, 659,
            698, 784, 880, 988, 1047, 1175, 1319, 1397, 1568, 1760,
            1976, 2093, 2349, 2637, 2959, 3322
        };
        int freq = letterFreqs[letterIndex];
        playTone(freq, 300, 0.8f);
    }

    //  File-based playback 

    /**
     * Play a sound file from the sounds/ directory.
     * @param fileName the filename (e.g., "cat sound.wav")
     * @return true if playback started successfully, false otherwise
     */
    public boolean playFileIfExists(String fileName) {
        if (soundsDir == null) return false;
        File f = new File(soundsDir, fileName);
        if (!f.exists()) {
            Logger.warn("Sound file not found: " + f.getAbsolutePath());
            return false;
        }
        return playFile(f.getAbsolutePath());
    }

    /**
     * Play a .wav sound file from an absolute path.
     * Uses Clip-based playback which handles WAV format natively.
     * Runs on a background thread to avoid blocking the UI.
     * @param filePath absolute path to the sound file
     * @return true if playback was initiated, false on error
     */
    public boolean playFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return false;
        File file = new File(filePath);
        if (!file.exists()) {
            Logger.warn("Sound file not found: " + filePath);
            return false;
        }
        ThreadPoolManager.execute(() -> {
            try {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                
                // Set volume
                if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    float dB = (float) (Math.log10(volume) * 20.0);
                    gainControl.setValue(Math.max(dB, gainControl.getMinimum()));
                }
                
                clip.start();
                
                // Wait for clip to finish playing, then clean up
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        try { audioIn.close(); } catch (Exception ignored) {}
                    }
                });
                
            } catch (javax.sound.sampled.UnsupportedAudioFileException e) {
                playWithVBS(filePath);
            } catch (Exception e) {
                Logger.error("File playback failed: " + filePath, e);
            }
        });
        return true;
    }

    private void playWithVBS(String filePath) {
        try {
            File vbsFile = new File(System.getProperty("java.io.tmpdir"), "play_audio_safehaven.vbs");
            if (!vbsFile.exists()) {
                try (PrintWriter writer = new PrintWriter(vbsFile)) {
                    writer.println("Set Sound = CreateObject(\"WMPlayer.OCX.7\")");
                    writer.println("Sound.URL = WScript.Arguments.Item(0)");
                    writer.println("Sound.settings.volume = " + (int)(volume * 100));
                    writer.println("Sound.Controls.play");
                    writer.println("WScript.Sleep 100");
                    writer.println("While Sound.playState <> 1");
                    writer.println("   WScript.Sleep 100");
                    writer.println("Wend");
                }
            }
            ProcessBuilder pb = new ProcessBuilder("wscript", vbsFile.getAbsolutePath(), filePath);
            Process p = pb.start();
            p.waitFor();
        } catch (Exception ex) {
            Logger.warn("VBScript fallback failed for " + filePath);
        }
    }

    //  Synthesized tone playback (PUBLIC - used by TextToSpeech) 

    public void playTone(int freq, int ms, float vol) {
        ThreadPoolManager.execute(() -> {
            try {
                int sr = 44100;
                int samples = sr * ms / 1000;
                byte[] buf = new byte[samples * 2];
                for (int i = 0; i < samples; i++) {
                    double t = (double)i / sr;
                    double env = Math.min(1.0, Math.min(t * 40, (ms/1000.0 - t) * 40));
                    short val = (short)(env * vol * volume * 32000 * Math.sin(2 * Math.PI * freq * t));
                    buf[2*i]   = (byte)(val & 0xFF);
                    buf[2*i+1] = (byte)((val >> 8) & 0xFF);
                }
                playRaw(buf, sr);
            } catch (Exception e) {
                Logger.error("Tone playback failed (freq=" + freq + ")", e);
            }
        });
    }

    private void playRaw(byte[] buf, int sampleRate) throws Exception {
        AudioFormat fmt = new AudioFormat(sampleRate, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
        if (!AudioSystem.isLineSupported(info)) return;
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(fmt);
        line.start();
        line.write(buf, 0, buf.length);
        line.drain();
        line.close();
    }

    public void delay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { 
            Logger.warn("Audio thread interrupted during delay");
            Thread.currentThread().interrupt();
        }
    }

    //  Helpers 

    /**
     * Finds the sounds/ directory by searching common locations.
     */
    private String findSoundsDir() {
        // Try relative to working directory
        String[] candidates = {
            "sounds",
            "SafeHaven_AutismAid/sounds",
            "../sounds",
            System.getProperty("user.dir") + "/sounds",
            System.getProperty("user.dir") + "/SafeHaven_AutismAid/sounds",
        };
        
        for (String path : candidates) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return dir.getAbsolutePath();
            }
        }
        
        // Try to find from classpath / source location
        try {
            String classPath = SoundManager.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI().getPath();
            File classDir = new File(classPath);
            // Go up from bin/ to project root
            File projectRoot = classDir.getParentFile();
            if (projectRoot != null) {
                File soundsFromRoot = new File(projectRoot, "sounds");
                if (soundsFromRoot.exists() && soundsFromRoot.isDirectory()) {
                    return soundsFromRoot.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            Logger.warn("Could not determine classpath for sounds directory: " + e.getMessage());
        }
        
        return null;
    }
}