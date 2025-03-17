import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class CustomTextField extends JTextField {
    private String placeholder; // Placeholder text
    private Timer animationTimer;
    private int[] yOffsets; // Offset for each letter
    private int frame = 0;

    public CustomTextField() {
        this(""); // Default constructor
    }

    public CustomTextField(String placeholder) {
        this.placeholder = (placeholder != null) ? placeholder : "";
        this.yOffsets = new int[this.placeholder.length()];

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });

        // Initialize animation timer
        animationTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (yOffsets.length != placeholder.length()) {
                    yOffsets = new int[placeholder.length()];
                }
                for (int i = 0; i < yOffsets.length; i++) {
                    yOffsets[i] = (int) (5 * Math.sin((frame + i) * 0.3)); // Wave effect
                }
                frame++;
                repaint();
            }
        });
        animationTimer.start();

        // Use a custom caret that does not paint anything (fully transparent)
        DefaultCaret caret = new DefaultCaret() {
            @Override
            public void paint(Graphics g) {
                // Do not paint anything, making the caret fully transparent
            }
        };
        caret.setBlinkRate(0); // Disable blinking
        setCaret(caret);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty() && placeholder != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.GRAY);
            g2d.setFont(getFont().deriveFont(Font.ITALIC));

            FontMetrics metrics = g2d.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(placeholder)) / 2;
            int baseY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

            // Draw each letter with animation
            for (int i = 0; i < placeholder.length(); i++) {
                g2d.drawString(String.valueOf(placeholder.charAt(i)), x, baseY + yOffsets[i]);
                x += metrics.charWidth(placeholder.charAt(i));
            }

            g2d.dispose();
        }
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = (placeholder != null) ? placeholder : "";
        this.yOffsets = new int[this.placeholder.length()]; // Resize offset array
        repaint();
    }
}