import java.awt.*;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public class WordThread extends Thread {
    private Font pixelatedEleganceFont;
    private Play play;
    private int wordLength;
    private WordGenerator wordGenerator;
    private static final int MAX_WORDS = 5;
    private static AtomicInteger currentWords = new AtomicInteger(0); // Thread-safe counter
    private Random random; // For generating random spawn delays
    private boolean stopGeneration = false; // Flag to stop word generation

    public WordThread(Play play, int wordLength, WordGenerator wordGenerator) {
        this.play = play;
        this.wordLength = wordLength;
        this.wordGenerator = wordGenerator;
        this.random = new Random(); // Initialize the Random object
    }

    @Override
    public void run() {
        while (!stopGeneration) { // Stop the loop if stopGeneration is true
            // Check if we can spawn a new word (thread-safe)
            if (currentWords.get() < MAX_WORDS) {
                String word = wordGenerator.getRandomWord(wordLength);
                if (word != null) {
                    // Increment the word count (thread-safe)
                    currentWords.incrementAndGet();
                    animateWord(word);
                }
            }

            // Generate a random delay between 500ms and 1500ms
            int randomDelay = random.nextInt(1001) + 200; // Random number between 500 and 1500 (inclusive)
            try {
                Thread.sleep(randomDelay); // Use the random delay
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void animateWord(String word) {
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

        JLabel wordLabel = new JLabel(word);
        wordLabel.setFont(pixelatedEleganceFont.deriveFont(Font.BOLD, 24));
        wordLabel.setForeground(Color.WHITE);

        // Calculate the width of the text
        FontMetrics metrics = wordLabel.getFontMetrics(wordLabel.getFont());
        int textWidth = metrics.stringWidth(word);

        // Set the bounds of the JLabel dynamically based on the text width
        int labelWidth = textWidth + 10; // Add some padding
        int labelHeight = 30; // Fixed height

        // Randomize the x position with 100px padding on both sides
        int screenWidth = 720; // Assuming the screen width is 720px
        int padding = 60; // Padding on both left and right sides
        int minX = padding; // Minimum x position (100px from the left)
        int maxX = screenWidth - padding - labelWidth; // Maximum x position (100px from the right, adjusted for label width)
        int xPosition = minX + random.nextInt(maxX - minX + 1); // Random x position within the padded area

        wordLabel.setBounds(xPosition, 0, labelWidth, labelHeight);

        play.addWordLabel(wordLabel);

        new Thread(() -> {
            while (wordLabel.getY() < 860 && !stopGeneration) { // Stop animation if stopGeneration is true
                // Check if the word label is still in the container
                if (play.containsWordLabel(wordLabel)) {
                    wordLabel.setLocation(wordLabel.getX(), wordLabel.getY() + 1);
                } else {
                    break; // Exit the loop if the word label has been removed
                }

                try {
                    Thread.sleep(5); // Adjust the speed of the animation
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Only stop the game if the word reaches the bottom (y-position 860)
            if (wordLabel.getY() >= 860 && play.containsWordLabel(wordLabel)) {
                stopGeneration = true; // Stop generating new words
                play.showGameOverMessage(); // Show the "Game Over" message
            }

            // Remove the word label and decrement the word count (thread-safe)
            play.removeWordLabel(wordLabel);
            currentWords.decrementAndGet();
        }).start();
    }

    public static void decrementWordCount() {
        currentWords.decrementAndGet();
    }

    public boolean isStopGeneration() {
        return stopGeneration;
    }
}