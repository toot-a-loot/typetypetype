import java.awt.event.KeyAdapter;
import javax.swing.*;

public class PlayTextField extends JTextField {
    private StackedSineWaveBackground background; // Reference to the background

    public PlayTextField(StackedSineWaveBackground background) {
        this.background = background; // Set the background reference
        setupKeyListener(); // Add key listener to trigger animation
    }

    private void setupKeyListener() {
        // Add a key listener to trigger the background animation when typing
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (isFocusOwner()) { // Only trigger if the PlayTextField has focus
                    background.triggerPulse(); // Trigger the background animation
                }
            }
        });
    }
}