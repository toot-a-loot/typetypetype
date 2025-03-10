import java.awt.*;
import javax.swing.*;

public class RoundedPanel extends JPanel {
    private int cornerRadius; // Radius for the rounded corners

    public RoundedPanel(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        setOpaque(false); // Make the panel transparent
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();

        // Enable anti-aliasing for smoother edges
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw the rounded rectangle
        g2d.setColor(getBackground());
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        g2d.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        // Optional: Draw a border around the rounded rectangle
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(getForeground());
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        g2d.dispose();
    }

    // public static void main(String[] args) {
    //     JFrame frame = new JFrame("Rounded Panel Example");
    //     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //     frame.setSize(400, 300);
    //     frame.setLocationRelativeTo(null);

    //     // Create a rounded panel
    //     RoundedPanel roundedPanel = new RoundedPanel(30); // 30px corner radius
    //     roundedPanel.setBackground(Color.white);
    //     roundedPanel.setForeground(Color.black); // Border color
    //     roundedPanel.setPreferredSize(new Dimension(200, 150));

    //     // Add the rounded panel to the frame
    //     frame.add(roundedPanel);
    //     frame.setVisible(true);
    // }
}