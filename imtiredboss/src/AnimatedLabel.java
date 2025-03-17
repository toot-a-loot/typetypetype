import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class AnimatedLabel extends JPanel {
    private String text = "Hello!";
    private int[] yOffsets;
    private Timer timer;
    private int frame = 0;

    public AnimatedLabel(String text) {
        this.text = text;
        this.yOffsets = new int[text.length()];
        
        timer = new Timer(100, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < yOffsets.length; i++) {
                    yOffsets[i] = (int) (3 * Math.sin((frame + i) * 0.7)); // Wavy effect
                }
                frame++;
                repaint();
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        int x = 20;
        
        for (int i = 0; i < text.length(); i++) {
            g.drawString(String.valueOf(text.charAt(i)), x, 50 + yOffsets[i]);
            x += fm.charWidth(text.charAt(i)) + 5;
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Animated JLabel");
        AnimatedLabel label = new AnimatedLabel("Hello!");
        frame.add(label);
        frame.setSize(300, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
