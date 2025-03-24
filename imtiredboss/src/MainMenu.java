import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class MainMenu extends JPanel 
{
    private Font pixelatedEleganceFont;
    private JPanel actualMainMenu;
    private JPanel topArea;
    private JPanel start;
    private BufferedImage settingsImageNormal;
    private BufferedImage settingsImageHover;
    private BufferedImage helpImageNormal;
    private BufferedImage helpImageHover;
    private SettingsIcon settingsIcon;
    private HelpIcon helpIcon;
    private JLabel typeToStartText;
    private JTextField inputField;
    private JLabel toBeginText;
    private JLayeredPane layerMyPanels;
    private RoundedPanel settingsPanel;
    private RoundedPanel helpPanel;

    public MainMenu() {
        loadCustomFont();
        setDoubleBuffered(true); // Enable double buffering
        setupHelpAndSettingsPanel();
        setupTopElements();
        setupBackground();
        setupStartPanel();
        
        
        layerMyPanels = new JLayeredPane();
        layerMyPanels.setBounds(0, 0, 720, 960);
        layerMyPanels.setLayout(null);

        layerMyPanels.add(actualMainMenu, JLayeredPane.DEFAULT_LAYER);
        layerMyPanels.add(settingsPanel, JLayeredPane.PALETTE_LAYER);
        layerMyPanels.add(helpPanel, JLayeredPane.PALETTE_LAYER);

        topArea.add(settingsIcon);
        topArea.add(helpIcon);
        actualMainMenu.add(topArea);
        actualMainMenu.add(start);

        add(layerMyPanels);

        
        setVisible(true);
    }

    private void setupBackground() {
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

        helpIcon = new HelpIcon();
        helpIcon.setBounds(630, 20, 32, 32); 
        
        helpIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                helpIcon.setHovered(true);
                helpIcon.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                helpIcon.setHovered(false);
                helpIcon.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO : make help
                showHelpPanel();
            }
        });

        settingsIcon = new SettingsIcon();
        settingsIcon.setBounds(670, 20, 32, 32); 
        
        settingsIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                settingsIcon.setHovered(true);
                settingsIcon.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                settingsIcon.setHovered(false);
                settingsIcon.repaint();
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                showSettingsPanel();
            }
        });
    }

    private void setupStartPanel() {
        start = new JPanel();
        start.setBounds(160, 350, 400, 200);
        start.setBackground(Color.BLACK);
        start.setLayout(new GridBagLayout());
        
        typeToStartText = new JLabel("Type");
        typeToStartText.setFont(pixelatedEleganceFont.deriveFont(Font.PLAIN, 14));
        typeToStartText.setForeground(Color.gray);
        typeToStartText.setHorizontalAlignment(SwingConstants.CENTER);
        
        inputField = new CustomTextField("START");
        inputField.setPreferredSize(new Dimension(70,50));
        inputField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        inputField.setColumns(5);
        inputField.setBackground(new Color(0, 0, 0, 0)); 
        inputField.setForeground(Color.WHITE);
        inputField.setFont(pixelatedEleganceFont.deriveFont(Font.BOLD, 32));
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setOpaque(false);

        toBeginText = new JLabel("to begin!");
        toBeginText.setFont(pixelatedEleganceFont.deriveFont(Font.PLAIN, 14));
        toBeginText.setForeground(Color.gray);
        toBeginText.setHorizontalAlignment(SwingConstants.CENTER);

        
        ((AbstractDocument) inputField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int newLength = currentText.length() - length + text.length();
                if (newLength <= 5) { 
                    super.replace(fb, offset, length, text.toUpperCase(), attrs);
                }
            }
    
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int newLength = currentText.length() + text.length();
                if (newLength <= 5) {
                    super.insertString(fb, offset, text.toUpperCase(), attrs);
                }
            }
        });
        
        inputField.addActionListener((ActionEvent e) -> {
            String input = inputField.getText().trim().toLowerCase();
            if (input.equals("start")) {
                // Get the parent window
                java.awt.Window window = SwingUtilities.getWindowAncestor(MainMenu.this);
                if (window != null) {
                    // Create new Play panel
                    Play play = new Play();
                    
                    // Start the word thread
                    WordGenerator wordGenerator = new WordGenerator("words.txt");
                    WordThread wordThread = new WordThread(play, 5, wordGenerator);
                    play.setWordThread(wordThread); 
                    wordThread.start();
                    
                    // Replace the content
                    ((JFrame) window).getContentPane().removeAll();
                    window.add(play);
                    window.revalidate();
                    window.repaint();
                    
                    // Request focus on the PlayTextField after the panel is visible
                    SwingUtilities.invokeLater(() -> {
                        play.getTypeHereField().requestFocusInWindow();
                    });
                }
            } else if (input.equals("exit")) {
                System.exit(0);
            } else {
                inputField.setText("");
            }
        });
            
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);
        
        start.add(typeToStartText, gbc);
        start.add(inputField, gbc);
        start.add(toBeginText, gbc);
    }
    
    private void setupHelpAndSettingsPanel()
    {
        helpPanel = new RoundedPanel(30);
        helpPanel.setBounds(85, 180, 550, 600);
        helpPanel.setBackground(Color.black);
        helpPanel.setForeground(Color.white); // Border color
        helpPanel.setVisible(false);

        settingsPanel = new RoundedPanel(30);
        settingsPanel.setBounds(85, 180, 550, 600);
        settingsPanel.setBackground(Color.black);
        settingsPanel.setForeground(Color.white); // Border color
        settingsPanel.setVisible(false);
    }

    private void setupTopElements() {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/settings.png");
            if (is == null) {
                throw new RuntimeException("Resource not found: /assets/settings.png");
            }
            
            BufferedImage fullImage = ImageIO.read(is);
            
            settingsImageNormal = fullImage.getSubimage(0, 0, 32, 32);
            settingsImageHover = fullImage.getSubimage(32, 0, 32, 32);

            is = getClass().getResourceAsStream("/assets/help.png");
            if (is == null) {
                throw new RuntimeException("Resource not found: /assets/settings.png");
            }

            fullImage = ImageIO.read(is);

            helpImageNormal = fullImage.getSubimage(0, 0, 32, 32);
            helpImageHover = fullImage.getSubimage(32, 0, 32, 32);

            
        } catch (IOException e) {
            System.out.println("Error loading settings image: " + e.getMessage());
        }
    }
    
    private class HelpIcon extends JPanel
    {
        private boolean hovered = false;

        public HelpIcon()
        {
            setPreferredSize(new Dimension(32,32));
            setOpaque(false);
        }

        public void setHovered(boolean hovered)
        {
            this.hovered = hovered;
        } 

        @Override
        protected void paintComponent(Graphics g)
        {
            g.setColor(new Color(0, 0, 0, 0)); 
            g.fillRect(0, 0, getWidth(), getHeight());

            if (hovered && helpImageHover != null) {
                g.drawImage(helpImageHover, 0, 0, this);
            } else if (helpImageNormal != null) {
                g.drawImage(helpImageNormal, 0, 0, this);
            }
        }
    }
    private class SettingsIcon extends JPanel 
    {
        private boolean hovered = false;
        
        public SettingsIcon() {
            setPreferredSize(new Dimension(32, 32));
            setOpaque(false); 
        }
        
        public void setHovered(boolean hovered) {
            this.hovered = hovered;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 0, 0)); 
            g.fillRect(0, 0, getWidth(), getHeight());
            
            if (hovered && settingsImageHover != null) {
                g.drawImage(settingsImageHover, 0, 0, this);
            } else if (settingsImageNormal != null) {
                g.drawImage(settingsImageNormal, 0, 0, this);
            }
        }
    }

    private void showSettingsPanel() {
        settingsPanel.setVisible(true);
        layerMyPanels.moveToFront(settingsPanel);
        disableBackgroundElements(true);
    }

    private void showHelpPanel() {
        helpPanel.setVisible(true);
        layerMyPanels.moveToFront(helpPanel);
        disableBackgroundElements(true);
    }

    private void disableBackgroundElements(boolean disable) {
        inputField.setEnabled(!disable);
        settingsIcon.setEnabled(!disable);
        helpIcon.setEnabled(!disable);
    }

    private void loadCustomFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/font/PixelatedElegance.ttf");
            if (is == null) {
                throw new RuntimeException("Font file not found: /fonts/PixelatedElegance.ttf");
            }
    
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            pixelatedEleganceFont = customFont.deriveFont(Font.PLAIN, 14); // Adjust size and style
        } catch (Exception e) {
            e.printStackTrace();
            pixelatedEleganceFont = new Font("SansSerif", Font.PLAIN, 14); // Fallback font
        }
    }
}