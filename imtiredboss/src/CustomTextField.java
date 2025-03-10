import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.*;

public class CustomTextField extends JTextField {
    private String placeholder; // The placeholder text

    public CustomTextField() {
        this(""); // Default constructor
    }

    public CustomTextField(String placeholder) {
        this.placeholder = placeholder;
        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint(); // Repaint when the field gains focus
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint(); // Repaint when the field loses focus
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the placeholder text if the field is empty
        if (getText().isEmpty()) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.GRAY); // Placeholder text color
            g2d.setFont(getFont().deriveFont(Font.ITALIC)); // Optional: Use italic font for placeholder

            // Calculate the position for the placeholder text
            FontMetrics metrics = g2d.getFontMetrics();
            Rectangle rect = getBounds(); // Get the bounds of the text field

            // Calculate the x position based on horizontal alignment
            int x;
            if (getHorizontalAlignment() == SwingConstants.CENTER) {
                // Center the placeholder text horizontally
                x = (rect.width - metrics.stringWidth(placeholder)) / 2;
            } else if (getHorizontalAlignment() == SwingConstants.RIGHT) {
                // Align the placeholder text to the right
                x = rect.width - metrics.stringWidth(placeholder) - getInsets().right;
            } else {
                // Default to left alignment
                x = getInsets().left;
            }

            // Calculate the y position to center the placeholder text vertically
            int y = (rect.height - metrics.getHeight()) / 2 + metrics.getAscent();

            g2d.drawString(placeholder, x, y);
            g2d.dispose();
        }
    }

    
    @Override
    protected void processKeyEvent(KeyEvent e) 
    {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
            if (getText().isEmpty()) {
                e.consume();
                return;
            }
        }
        super.processKeyEvent(e);
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint(); // Repaint to update the placeholder text
    }
}