import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    private List<JLabel> wordLabels; // List of active word labels
    private List<String> activeWords; // Track active words
    private WordThread wordThread; // Reference to the WordThread

    public Play() {
        wordLabels = new ArrayList<>();
        activeWords = new ArrayList<>(); // Initialize the list of active words
        loadCustomFont();
        setupPlayer();
        setupBackground();
    }

    private void setupBackground() {
        this.setBounds(0, 0, 720, 960);
        this.setLayout(null);

        backPanel = new StackedSineWaveBackground(720, 960);
        backPanel.setBounds(0, 0, 720, 960);

        // Create the PlayTextField and pass the background instance
        typeHere = new PlayTextField(backPanel); // Use PlayTextField instead of CustomTextField
        typeHere.setOpaque(false);
        typeHere.setForeground(Color.white);
        typeHere.setBackground(new Color(0, 0, 0, 0));
        typeHere.setFont(pixelatedEleganceFont.deriveFont(Font.PLAIN, 24));
        typeHere.setColumns(20);
        typeHere.setBorder(new MatteBorder(0, 0, 1, 0, Color.white));
        typeHere.setBounds(158, 740, 400, 50);
        typeHere.setHorizontalAlignment(JTextField.CENTER);

        // Set focus on the PlayTextField when the program launches
        typeHere.requestFocusInWindow();

        // Add an ActionListener to handle the Enter key press
        typeHere.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (wordThread.isStopGeneration()) {
                    return; // Do nothing if word generation has stopped
                }

                String input = typeHere.getText().trim().toUpperCase();
                for (int i = 0; i < activeWords.size(); i++) {
                    if (input.equals(activeWords.get(i))) {
                        // Match found, remove the word label
                        JLabel wordLabel = wordLabels.get(i);
                        removeWordLabel(wordLabel); // Remove the word label from the container
                        typeHere.setText(""); // Clear the input field
                        WordThread.decrementWordCount(); // Decrement the word count
                        break;
                    }
                }
            }
        });

        // Apply document filter to limit input to 20 characters and enforce uppercase
        ((AbstractDocument) typeHere.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int newLength = currentText.length() - length + text.length();
                if (newLength <= 20) { // Limit to 20 characters
                    super.replace(fb, offset, length, text.toUpperCase(), attrs); // Convert to uppercase
                }
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                int newLength = currentText.length() + text.length();
                if (newLength <= 20) { // Limit to 20 characters
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
                    g.drawImage(player.getSubimage(0, 0, 62, 62), 327, 800, null);
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

        // Add the text field to the layered pane
        layerMyPanels.add(typeHere, JLayeredPane.MODAL_LAYER);

        add(layerMyPanels);
        this.setVisible(true);
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
        wordLabels.add(wordLabel);
        activeWords.add(wordLabel.getText()); // Add the word to the active words list
        layerMyPanels.add(wordLabel, JLayeredPane.MODAL_LAYER);
        layerMyPanels.revalidate();
        layerMyPanels.repaint();
    }

    public void removeWordLabel(JLabel wordLabel) {
        wordLabels.remove(wordLabel);
        activeWords.remove(wordLabel.getText()); // Remove the word from the active words list
        layerMyPanels.remove(wordLabel); // Remove the word label from the container
        layerMyPanels.revalidate();
        layerMyPanels.repaint();
        WordThread.decrementWordCount(); // Decrement the word count
    }

    public boolean containsWordLabel(JLabel wordLabel) {
        return wordLabels.contains(wordLabel); // Check if the word label is still in the container
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

    public void setWordThread(WordThread wordThread) {
        this.wordThread = wordThread;
    }

    public void showGameOverMessage() {
        JLabel gameOverLabel = new JLabel("Game Over!");
        gameOverLabel.setFont(pixelatedEleganceFont.deriveFont(Font.BOLD, 48));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setBounds(200, 400, 400, 100);
        layerMyPanels.add(gameOverLabel, JLayeredPane.MODAL_LAYER);
        layerMyPanels.revalidate();
        layerMyPanels.repaint();
    }
}