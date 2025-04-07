import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;
import javax.swing.*;

public class PlayTextField extends JTextField {
    private StackedSineWaveBackground background;
    private Runnable onTypeCallback;
    private Clip audioClip;
    private float volume = 0.7f;
    
    public PlayTextField(StackedSineWaveBackground background) {
        this.background = background;
        loadAudio();
        setupKeyListener();
    }
    
    public void setOnTypeCallback(Runnable callback) {
        this.onTypeCallback = callback;
    }
    
    private void loadAudio() {
        try {
            // Load the sound file as a resource from the classpath
            URL soundURL = getClass().getClassLoader().getResource("music/click.wav");
            
            if (soundURL == null) {
                System.err.println("Could not find sound file: music/click.wav");
                return;
            }
            
            // Get audio input stream from the URL
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            
            // Get a clip resource
            audioClip = AudioSystem.getClip();
            
            // Open audio clip and load samples from the audio input stream
            audioClip.open(audioIn);
            
            // Set initial volume
            updateVolume();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error loading sound: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public float getVolume() {
        return volume;
    }
    
    private void updateVolume() {
        if (audioClip != null && audioClip.isOpen()) {
            try {
                FloatControl gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
                if (gainControl != null) {
                    // Convert the linear volume (0.0 to 1.0) to decibels
                    float dB = 20.0f * (float) Math.log10(volume);
                    // Clamp to the allowed range
                    dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                    gainControl.setValue(dB);
                }
            } catch (IllegalArgumentException e) {
                // Control not supported
                System.err.println("Volume control not supported: " + e.getMessage());
            }
        }
    }
    
    private void playTypeSound() {
        if (audioClip != null) {
            // Stop the clip if it's still playing
            if (audioClip.isRunning()) {
                audioClip.stop();
            }
            // Reset to the beginning
            audioClip.setFramePosition(0);
            // Start playing
            audioClip.start();
        }
    }
    
    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (isFocusOwner()) {
                    background.triggerPulse();
                    playTypeSound();
                    if (onTypeCallback != null) {
                        onTypeCallback.run();
                    }
                }
            }
        });
    }
}