import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.*;

public class MusicPlayer {
    private Clip clip;
    private boolean isPlaying = false;
    private FloatControl volumeControl;
    private float volume = 0.5f; // Default volume (50%)

       public void play(String resourcePath, boolean loop) {
        stop();
        
        try {
            InputStream audioSrc = getClass().getResourceAsStream(resourcePath);
            if (audioSrc == null) {
                System.err.println("Could not find audio file: " + resourcePath);
                return;
            }
            
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioSrc);
            AudioFormat format = audioInputStream.getFormat();
            
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Audio format not supported: " + format);
                return;
            }
            
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioInputStream);
            
            try {
                volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(volume);
            } catch (Exception e) {
                System.err.println("Volume control not available: " + e.getMessage());
            }

            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }

            clip.start();
            isPlaying = true;
            
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing audio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (clip != null && clip.isOpen()) {
            clip.stop();
            clip.close();
            isPlaying = false;
        }
    }
    
    public void setVolume(float volume) {
        if (volume < 0f) volume = 0f;
        if (volume > 1f) volume = 1f;
        
        this.volume = volume;
        
        if (volumeControl != null) {
            float range = volumeControl.getMaximum() - volumeControl.getMinimum();
            float gain;
            
            if (volume > 0f) {
                gain = (range * volume) + volumeControl.getMinimum();
            } else {
                gain = volumeControl.getMinimum();
            }
            
            volumeControl.setValue(gain);
        }
    }
    
    public boolean isPlaying() {
        return isPlaying;
    }
}