import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class Play extends JPanel
{
    private JPanel backPanel;
    private BufferedImage player;
    private JPanel ground;
    private JLayeredPane layerMyPanels;

    public Play()
    {
        setupPlayer();
        setupBackground();
    }

    private void setupBackground()
    {
        this.setBounds(0,0,720,960);
        this.setLayout(null);
        
        backPanel = new StackedSineWaveBackground(720, 960);
        backPanel.setBounds(0,0,720,960);

        ground = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.black);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        ground.setBounds(0, 860, 720, 100);
        
        // Create a panel for the player
        JPanel playerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (player != null) {
                    g.drawImage(player.getSubimage(0, 0, 62, 62), 340, 764, null);
                }
            }
        };
        playerPanel.setBounds(0, 0, 720, 960);
        playerPanel.setOpaque(false);
        
        layerMyPanels = new JLayeredPane();
        layerMyPanels.setBounds(0, 0, 720, 960);
        layerMyPanels.setLayout(null);

        layerMyPanels.add(backPanel, JLayeredPane.DEFAULT_LAYER);
        layerMyPanels.add(ground, JLayeredPane.PALETTE_LAYER);
        layerMyPanels.add(playerPanel, JLayeredPane.DRAG_LAYER);

        add(layerMyPanels);
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

    // public void paintComponent(Graphics g)
    // {
    //     super.paintComponent(g);    

    //     g.drawImage(player.getSubimage(0, 0, 96, 96), 0, 0, null);
    // }
}
