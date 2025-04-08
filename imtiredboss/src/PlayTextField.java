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
            URL soundURL = getClass().getClassLoader().getResource("music/click.wav");
            
            if (soundURL == null) {
                System.err.println("Could not find sound file: music/click.wav");
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);

            audioClip = AudioSystem.getClip();

            audioClip.open(audioIn);

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
                    float dB = 20.0f * (float) Math.log10(volume);
                    dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                    gainControl.setValue(dB);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Volume control not supported: " + e.getMessage());
            }
        }
    }
    
    private void playTypeSound() {
        if (audioClip != null) {
            if (audioClip.isRunning()) {
                audioClip.stop();
            }
            audioClip.setFramePosition(0);
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