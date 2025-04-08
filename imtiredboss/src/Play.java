import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public class Play extends JPanel {
    private Font pixelatedEleganceFont;
    private StackedSineWaveBackground backPanel;
    private BufferedImage player;
    private JPanel ground;
    private JLayeredPane layerMyPanels;
    private PlayTextField typeHere;
    private List<JLabel> wordLabels = Collections.synchronizedList(new ArrayList<>());
    private List<String> activeWords = Collections.synchronizedList(new ArrayList<>());
    private WordThread wordThread;
    private int currentSpriteFrame = 0; // Track current sprite frame
    private final int SPRITE_WIDTH = 62;
    private final int SPRITE_HEIGHT = 62;
    private final Object wordLabelLock = new Object();
    private MusicPlayer musicPlayer;
    private JLabel gameOverContainer;
    private String gameplayMusicPath = "/music/ingame music (k.k slider).wav"; 

    public Play() {
        loadCustomFont();
        setupPlayer();
        setupBackground();
        initializeMusic();
    }

    private void initializeMusic() {
        musicPlayer = new MusicPlayer();
        
        musicPlayer.play(gameplayMusicPath, true); 
        musicPlayer.setVolume(0.8f);
    }

    private void setupBackground() {
        this.setBounds(0, 0, 720, 960);
        this.setLayout(null);

        backPanel = new StackedSineWaveBackground(720, 960);
        backPanel.setBounds(0, 0, 720, 960);

        typeHere = new PlayTextField(backPanel);
        typeHere.setOpaque(false);
        typeHere.setForeground(Color.white);
        typeHere.setBackground(new Color(0, 0, 0, 0));
        typeHere.setFont(pixelatedEleganceFont.deriveFont(Font.PLAIN, 24));
        typeHere.setColumns(20);
        typeHere.setBorder(new MatteBorder(0, 0, 1, 0, Color.white));
        typeHere.setBounds(158, 740, 400, 50);
        typeHere.setHorizontalAlignment(JTextField.CENTER);

        typeHere.requestFocusInWindow();

        typeHere.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (wordThread.isStopGeneration()) {
                    String input = typeHere.getText().trim().toUpperCase();

                    if (input.equals("RESTART")) {
                        restartGame();
                    } 
                    else if (input.equals("MAIN MENU")) {
                        returnToMainMenu();
                    }
                    else if (input.equals("EXIT")) {
                        exitGame();
                    }
                    typeHere.setText("");
                    return;
                }

                String input = typeHere.getText().trim().toUpperCase();
                
                synchronized (wordLabelLock) {
                    for (int i = 0; i < activeWords.size(); i++) {
                        if (input.equals(activeWords.get(i))) {
                            JLabel wordLabel = wordLabels.get(i);
                            removeWordLabel(wordLabel);
                            typeHere.setText("");
                            WordThread.decrementWordCount();
                            break;
                        }
                    }
                }
            }
        });

        ((AbstractDocument) typeHere.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int newLength = currentText.length() - length + text.length();
                if (newLength <= 20) {
                    super.replace(fb, offset, length, text.toUpperCase(), attrs);
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int newLength = currentText.length() + text.length();
                if (newLength <= 20) {
                    super.insertString(fb, offset, text.toUpperCase(), attrs);
                }
            }
        });

        ground = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(30, 30, 30));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        ground.setBounds(0, 860, 720, 100);

        JPanel playerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (player != null) {
                    int srcX = currentSpriteFrame * SPRITE_WIDTH;
                    // Ensure we don't go out of bounds
                    srcX = Math.min(srcX, player.getWidth() - SPRITE_WIDTH);
                    g.drawImage(player.getSubimage(srcX, 0, SPRITE_WIDTH, SPRITE_HEIGHT), 
                               327, 800, null);
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

        layerMyPanels.add(typeHere, JLayeredPane.MODAL_LAYER);

        add(layerMyPanels);

        this.setVisible(true);
        typeHere.setOnTypeCallback(() -> {
            currentSpriteFrame = (int)(Math.random() * 2) + 1;
            playerPanel.repaint();

            Timer timer = new Timer(300, evt -> {
                currentSpriteFrame = 0;
                playerPanel.repaint();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    private void setupPlayer() {
        InputStream is = getClass().getResourceAsStream("/assets/main character.png");
        try {
            player = ImageIO.read(is);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void addWordLabel(JLabel wordLabel) {
        synchronized (wordLabelLock) {
            wordLabels.add(wordLabel);
            activeWords.add(wordLabel.getText());
            layerMyPanels.add(wordLabel, JLayeredPane.MODAL_LAYER);
            layerMyPanels.revalidate();
            layerMyPanels.repaint();
        }
    }

    public void removeWordLabel(JLabel wordLabel) {
        synchronized (wordLabelLock) {
            int index = wordLabels.indexOf(wordLabel);
            if (index != -1) {
                activeWords.remove(index);
                wordLabels.remove(index);
                layerMyPanels.remove(wordLabel);
                layerMyPanels.revalidate();
                layerMyPanels.repaint();
            }
        }
    }

    public boolean containsWordLabel(JLabel wordLabel) {
        synchronized (wordLabelLock) {
            return wordLabels.contains(wordLabel);
        }
    }

    private void loadCustomFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/font/PixelatedElegance.ttf");
            if (is == null) {
                throw new RuntimeException("Font file not found: /fonts/PixelatedElegance.ttf");
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            pixelatedEleganceFont = customFont.deriveFont(Font.PLAIN, 14);
        } catch (Exception e) {
            e.printStackTrace();
            pixelatedEleganceFont = new Font("SansSerif", Font.PLAIN, 14);
        }
    }

    public void setWordThread(WordThread wordThread) {
        this.wordThread = wordThread;
    }

    public PlayTextField getTypeHereField() {
        return typeHere;
    }

    public void showGameOverMessage() {
        gameOverContainer = new JLabel();
        gameOverContainer.setLayout(null);
        gameOverContainer.setBounds(120, 200, 500, 600);

        JLabel gameOverLabel = new JLabel("Game Over :(");
        gameOverLabel.setFont(pixelatedEleganceFont.deriveFont(Font.BOLD, 48));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setBounds(0, 0, 500, 100);
        gameOverLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setOpaque(false); // if you want transparent background

        String[] lines = { "Type", "", "RESTART", "MAIN MENU", "or", "EXIT" };
        for (String line : lines)
        {
            JLabel label = new JLabel(line);
            label.setFont(pixelatedEleganceFont.deriveFont(Font.BOLD, 24f));
            label.setForeground(Color.GRAY);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            messagePanel.add(label);
        }

        messagePanel.setBounds(0, 70, 500, 300);

        gameOverContainer.add(gameOverLabel);
        gameOverContainer.add(messagePanel);

        layerMyPanels.add(gameOverContainer, JLayeredPane.MODAL_LAYER);
        layerMyPanels.revalidate();
        layerMyPanels.repaint();

        typeHere.requestFocusInWindow();
    }

    public void restartGame() {
        if (gameOverContainer != null) {
            layerMyPanels.remove(gameOverContainer);
            gameOverContainer = null;
        }

        synchronized (wordLabelLock) {
            for (JLabel label : wordLabels) {
                layerMyPanels.remove(label);
            }
            wordLabels.clear();
            activeWords.clear();
        }

        layerMyPanels.revalidate();
        layerMyPanels.repaint();

        if (wordThread != null) {
            wordThread.restart();
        }

        typeHere.requestFocusInWindow();
    }

    public void returnToMainMenu() 
    {
        stopMusic();
        java.awt.Window window = SwingUtilities.getWindowAncestor(Play.this);
        if (window != null) 
        {
            MainMenu mainMenu = new MainMenu();
            
            
            ((JFrame) window).getContentPane().removeAll();
            window.add(mainMenu);
            window.revalidate();
            window.repaint();
            
            SwingUtilities.invokeLater(() -> {
                mainMenu.getInputField().requestFocusInWindow();
            });
        }
        
    }
    
    public void exitGame() {
        System.out.println("byebye :(");
        System.exit(0);
    }
}