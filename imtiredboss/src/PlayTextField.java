import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class PlayTextField extends JTextField {
    private StackedSineWaveBackground background;
    private Runnable onTypeCallback;

    public PlayTextField(StackedSineWaveBackground background) {
        this.background = background;
        setupKeyListener();
    }

    public void setOnTypeCallback(Runnable callback) {
        this.onTypeCallback = callback;
    }

    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (isFocusOwner()) {
                    background.triggerPulse();
                    if (onTypeCallback != null) {
                        onTypeCallback.run();
                    }
                }
            }
        });
    }
}