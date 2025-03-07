import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class MainMenu extends JPanel
{
    private JPanel actualMainMenu;
    private JPanel topArea;
    private JPanel start;
    private BufferedImage settingsImageNormal;
    private BufferedImage settingsImageHover;
    private SettingsPanel settingsPanel;

    public MainMenu()
    {
        setupSettings();
        setupBackground();
        setupStartPanel();
        
        // Add all components to their parent containers
        topArea.add(settingsPanel);
        actualMainMenu.add(topArea);
        actualMainMenu.add(start);
        add(actualMainMenu);
        
        setVisible(true);
    }

    private void setupBackground()
    {
        this.setBounds(0, 0, 720, 960);
        this.setBackground(Color.BLACK);
        this.setLayout(null);
        
        actualMainMenu = new JPanel();
        actualMainMenu.setBounds(0, 0, 720, 960);
        actualMainMenu.setBackground(Color.black);
        actualMainMenu.setLayout(null);

        topArea = new JPanel();
        topArea.setBounds(0, 0, 720, 100);
        topArea.setBackground(Color.black);
        topArea.setLayout(null);
        
        settingsPanel = new SettingsPanel();
        settingsPanel.setBounds(670, 20, 32, 32); 
        
        settingsPanel.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mouseEntered(MouseEvent e) {
                settingsPanel.setHovered(true);
                settingsPanel.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                settingsPanel.setHovered(false);
                settingsPanel.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO : make settings
            }
        });
    }

    private void setupStartPanel() 
    {
        // Create start section with centered components
        start = new JPanel();
        start.setBounds(160, 400, 400, 200);
        start.setBackground(Color.BLACK);
        start.setLayout(new GridBagLayout());
        
        // Create UI components with instructions
        JLabel instructionLabel = new JLabel("Type 'start' to begin");
        instructionLabel.setForeground(Color.WHITE);
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        final JTextField inputField = new JTextField(10);
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputField.setBackground(new Color(0, 0, 0, 0));
        inputField.setForeground(Color.WHITE);
        inputField.setHorizontalAlignment(JTextField.CENTER);
        
        inputField.addActionListener((ActionEvent e) -> {
            String input = inputField.getText().trim().toLowerCase();
            if (input.equals("start")) {
                // Just commented out as per your request
                // You can add your game launch logic here later
            } else {
                inputField.setText("");
            }
        });
        
        // TODO: FOR ADDITIONAL FEATURES BELOW

        // Also check input while typing (optional, for immediate feedback)
        // inputField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        //     public void changedUpdate(javax.swing.event.DocumentEvent e) { checkInput(); }
        //     public void removeUpdate(javax.swing.event.DocumentEvent e) { checkInput(); }
        //     public void insertUpdate(javax.swing.event.DocumentEvent e) { checkInput(); }
            
        //     public void checkInput() {
        //         String text = inputField.getText().trim().toLowerCase();
        //         if (text.equals("start")) {
        //             inputField.setForeground(Color.GREEN); // Visual feedback
        //         } else {
        //             inputField.setForeground(Color.WHITE);
        //         }
        //     }
        // });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        
        start.add(instructionLabel, gbc);
        start.add(inputField, gbc);
    }

    private void setupSettings()
    {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/settings.png");
            if (is == null) {
                throw new RuntimeException("Resource not found: /assets/settings.png");
            }
            
            BufferedImage fullImage = ImageIO.read(is);
            
            settingsImageNormal = fullImage.getSubimage(0, 0, 32, 32);
            
            settingsImageHover = fullImage.getSubimage(32, 0, 32, 32);
            
        } catch (IOException e) {
            System.out.println("Error loading settings image: " + e.getMessage());
        }
    }
    
    private class SettingsPanel extends JPanel {
        private boolean hovered = false;
        
        public SettingsPanel() {
            setPreferredSize(new Dimension(32, 32));
            setOpaque(false); 
        }
        
        public void setHovered(boolean hovered) {
            this.hovered = hovered;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (hovered && settingsImageHover != null) {
                g.drawImage(settingsImageHover, 0, 0, this);
            } else if (settingsImageNormal != null) {
                g.drawImage(settingsImageNormal, 0, 0, this);
            }
        }
    }
}