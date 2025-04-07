import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.InputStream;
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
    private JLabel typeToStartText;
    private CustomTextField inputField;
    private JLabel toBeginText;
    private JLayeredPane layerMyPanels;
    private RoundedPanel settingsPanel;
    private RoundedPanel helpPanel;
    private MusicPlayer musicPlayer; 
    private String backgroundMusicPath = "music/main menu bgm (undertale ost no. 20).wav"; 

    public MainMenu() {
        loadCustomFont();
        setDoubleBuffered(true); 
        setupHelpAndSettingsPanel();
        setupBackground();
        setupStartPanel();
        initializeMusic(); 
        
        layerMyPanels = new JLayeredPane();
        layerMyPanels.setBounds(0, 0, 720, 960);
        layerMyPanels.setLayout(null);

        layerMyPanels.add(actualMainMenu, JLayeredPane.DEFAULT_LAYER);
        layerMyPanels.add(settingsPanel, JLayeredPane.PALETTE_LAYER);
        layerMyPanels.add(helpPanel, JLayeredPane.PALETTE_LAYER);

        actualMainMenu.add(topArea);
        actualMainMenu.add(start);

        add(layerMyPanels);
        
        setVisible(true);
    }

    private void initializeMusic() {
        musicPlayer = new MusicPlayer();
        musicPlayer.play(backgroundMusicPath, true);
        musicPlayer.setVolume(1.0f);
    }
    
    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    public void setBackgroundMusicPath(String path) {
        this.backgroundMusicPath = path;
        if (musicPlayer != null && musicPlayer.isPlaying()) {
            musicPlayer.stop();
            musicPlayer.play(backgroundMusicPath, true);
        }
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
                stopMusic(); // Stop music before transitioning
                
                java.awt.Window window = SwingUtilities.getWindowAncestor(MainMenu.this);
                if (window != null) 
                {
                    Play play = new Play();
                    
                    WordGenerator wordGenerator = new WordGenerator("src/words.txt");
                    WordThread wordThread = new WordThread(play, 5, wordGenerator);
                    play.setWordThread(wordThread); 
                    wordThread.start();
                    
                    ((JFrame) window).getContentPane().removeAll();
                    window.add(play);
                    window.revalidate();
                    window.repaint();
                    
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
    
    // Rest of the MainMenu class code...
    // (unchanged methods omitted for brevity)
    
    private void setupHelpAndSettingsPanel() {
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

    public CustomTextField getInputField() {
        return inputField;
    }
}