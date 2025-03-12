import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class HorizontalCaret extends DefaultCaret {
    @Override
    protected synchronized void damage(Rectangle r) {
        if (r == null) return;

        // Make the caret a horizontal line
        x = r.x - 10;
        y = r.y + r.height - 8; // Adjust the y position to be at the bottom of the text
        width = 10; // Custom caret length
        height = 3; // Height of the horizontal caret

        repaint();
    }

    @Override
    public void paint(Graphics g) {
        if (isVisible()) {
            JTextField textField = (JTextField) getComponent();
            Graphics2D g2d = (Graphics2D) g.create();

            // Set the caret color
            g2d.setColor(Color.white);

            // Draw the horizontal caret
            g2d.fillRect(x, y, width, height);

            g2d.dispose();
        }
    }
}