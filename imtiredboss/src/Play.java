import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Play extends JPanel
{
    private JPanel actualPlayArea;
    private BufferedImage player;
    private JPanel ground;

    public Play()
    {
        setupPlayer();
        setupBackground();
    }

    private void setupBackground()
    {
        this.setBounds(0,0,720,960);
        this.setBackground(Color.BLACK);
        this.setLayout(null);
        
        actualPlayArea = new JPanel();
        actualPlayArea.setBounds(0,0,720,960);
        actualPlayArea.setBackground(Color.lightGray);

        ground = new JPanel();
        ground.setBounds(0,860,720,100);
        ground.setBackground(Color.DARK_GRAY);

        this.add(ground);
        this.add(actualPlayArea);
        this.setVisible(true);
    }

    private void setupPlayer()
    {
        InputStream is = getClass().getResourceAsStream("/assets/main character.png");

        try 
        {
            player = ImageIO.read(is);
        } catch (IOException ex) 
        {

        }

    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);    

        g.drawImage(player.getSubimage(0, 0, 96, 96), 0, 0, null);
    }
}
