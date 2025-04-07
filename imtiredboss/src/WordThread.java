import java.awt.*;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.*;

public class WordThread extends Thread {
    private Font pixelatedEleganceFont;
    private Play play;
    private int wordLength;
    private WordGenerator wordGenerator;
    private static final int MAX_WORDS = 5;
    private Random random;
    private volatile boolean stopGeneration = false;
    
    // Static variables for tracking words across all instances
    private static volatile int activeWordCount = 0;
    private static volatile boolean batchInProgress = false;
    private static final Object batchLock = new Object();
    
    // Delay between individual word spawns (in milliseconds)
    private static final int MIN_WORD_SPAWN_DELAY = 200;
    private static final int MAX_WORD_SPAWN_DELAY = 600;
    
    // Thread pools to manage concurrent tasks efficiently
    private static final ExecutorService wordAnimationPool = Executors.newFixedThreadPool(8);
    
    // Controller thread reference for restart functionality
    private Thread controllerThread;
    private volatile boolean isRunning = false;

    public WordThread(Play play, int wordLength, WordGenerator wordGenerator) {
        this.play = play;
        this.wordLength = wordLength;
        this.wordGenerator = wordGenerator;
        this.random = new Random();
        
        loadCustomFont();
    }
    
    private void loadCustomFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/font/PixelatedElegance.ttf");
            if (is == null) {
                throw new RuntimeException("Font file not found: /fonts/PixelatedElegance.ttf");
            }

            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            pixelatedEleganceFont = customFont.deriveFont(Font.PLAIN, 14);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            pixelatedEleganceFont = new Font("SansSerif", Font.PLAIN, 14);
        }
    }

    @Override
    public void run() {
        isRunning = true;
        startControllerThread();
    }
    
    private void startControllerThread() {
        controllerThread = new Thread(() -> {
            synchronized (batchLock) {
                activeWordCount = 0;
                batchInProgress = false;
            }
            
            while (!stopGeneration && isRunning) {
                try {
                    // Wait until all active words are cleared before starting a new batch
                    waitForWordsClear();
                    
                    if (stopGeneration || !isRunning) break;
                    
                    // Start a new batch of words
                    spawnWordBatch();
                    
                    // Add some delay between batches
                    // Thread.sleep(random.nextInt(1001) + 400);
                } catch (InterruptedException e) {
                    if (stopGeneration || !isRunning) break;
                    e.printStackTrace();
                }
            }
        });
        controllerThread.start();
    }
    
    private void waitForWordsClear() throws InterruptedException {
        synchronized (batchLock) {
            // Wait until there are no active words and no batch is in progress
            while ((activeWordCount > 0 || batchInProgress) && isRunning) {
                if (stopGeneration) return;
                batchLock.wait(100); // Wake up periodically to check stopGeneration
            }
        }
    }
    
    private void spawnWordBatch() {
        synchronized (batchLock) {
            // Set the flag to prevent another batch from starting
            batchInProgress = true;
            // Reset the active word count for this batch
            activeWordCount = MAX_WORDS;
        }
        
        // Spawn each word with a delay
        for (int i = 0; i < MAX_WORDS; i++) {
            final int threadId = i;
            
            // Use a new thread for spawning as in the original implementation
            new Thread(() -> {
                try {
                    // Add delay between spawning each word
                    if (threadId > 0) {
                        int delay = MIN_WORD_SPAWN_DELAY + random.nextInt(MAX_WORD_SPAWN_DELAY - MIN_WORD_SPAWN_DELAY + 1);
                        Thread.sleep(delay * threadId);
                    }
                    
                    // Check if we should stop before generating a word
                    if (stopGeneration || !isRunning) {
                        decrementWordSafely();
                        return;
                    }
                    
                    // Generate and animate a word
                    String word = wordGenerator.getRandomWord(wordLength);
                    if (word != null && !stopGeneration && isRunning) {
                        animateWord(word);
                    } else {
                        // If no word was generated, still decrement
                        decrementWordSafely();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // Ensure count is decremented even on exception
                    decrementWordSafely();
                }
            }).start();
        }
    }

    // Helper method to safely decrement word count
    private void decrementWordSafely() {
        synchronized (batchLock) {
            activeWordCount--;
            batchInProgress = true;
            if (activeWordCount <= 0) {
                batchInProgress = false;
                batchLock.notifyAll();
            }
        }
    }

    private void animateWord(String word) {
        JLabel wordLabel = new JLabel(word);
        wordLabel.setFont(pixelatedEleganceFont.deriveFont(Font.BOLD, 24));
        wordLabel.setForeground(Color.WHITE);

        // Calculate the width of the text
        FontMetrics metrics = wordLabel.getFontMetrics(wordLabel.getFont());
        int textWidth = metrics.stringWidth(word);

        // Set the bounds of the JLabel dynamically based on the text width
        int labelWidth = textWidth + 10;
        int labelHeight = 30;

        // Randomize the x position with padding on both sides
        int screenWidth = 720;
        int padding = 60;
        int minX = padding;
        int maxX = screenWidth - padding - labelWidth;
        int xPosition = minX + random.nextInt(maxX - minX + 1);

        // Set initial Y position to be visible in the game area (20px from top)
        int initialY = 20;
        wordLabel.setBounds(xPosition, initialY, labelWidth, labelHeight);

        // Add the word label to the UI on the EDT
        SwingUtilities.invokeLater(() -> {
            // Only add the label if we're still running
            if (!stopGeneration && isRunning) {
                play.addWordLabel(wordLabel);
            } else {
                // If we've stopped, don't add the label but decrement the count
                decrementWordSafely();
                return;
            }
        });

        // Use the thread pool for the animation part
        wordAnimationPool.execute(() -> {
            final boolean[] wordRemoved = {false};
            
            // Keep track of position locally within this thread
            int yPos = initialY;
            
            while (yPos < 840 && !stopGeneration && isRunning) {
                final int currentY = yPos;  // Create final copy for lambda
                
                // Check if the word label is still in the container
                boolean stillPresent = false;
                try {
                    final boolean[] checkResult = {false};
                    SwingUtilities.invokeAndWait(() -> {
                        checkResult[0] = play.containsWordLabel(wordLabel);
                        // Only update position if the label is still present
                        if (checkResult[0]) {
                            wordLabel.setLocation(wordLabel.getX(), currentY);
                        }
                    });
                    stillPresent = checkResult[0];
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (!stillPresent) {
                    // Word was typed correctly or removed elsewhere
                    if (!wordRemoved[0]) {
                        wordRemoved[0] = true;
                        // No need to call decrementWordCount() here as it's called in Play class
                    }
                    return; // Exit the thread
                }
                
                // Increment position for next iteration
                yPos++;
                
                try {
                    Thread.sleep(6); // Adjust the speed of the animation
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break; // Exit on interruption
                }
            }

            // Handle game over condition
            final int finalY = yPos;
            SwingUtilities.invokeLater(() -> {
                // Only stop the game if the word reaches the bottom (y-position 840)
                if (finalY >= 840 && play.containsWordLabel(wordLabel) && isRunning) {
                    stopGeneration = true; // Stop generating new words
                    
                    // Notify any waiting threads to wake up and check stopGeneration
                    synchronized (batchLock) {
                        batchLock.notifyAll();
                    }
                    
                    play.showGameOverMessage(); // Show the "Game Over" message
                }

                // Remove the word label if it wasn't already removed
                if (play.containsWordLabel(wordLabel)) {
                    wordRemoved[0] = true;
                    play.removeWordLabel(wordLabel);
                    // Decrement here since this wasn't typed correctly but reached bottom or game ended
                    decrementWordSafely();
                }
            });
        });
    }

    // This public static method is needed for the Play class
    public static void decrementWordCount() {
        synchronized (batchLock) {
            activeWordCount--;
            batchInProgress = true;
            if (activeWordCount <= 0) {
                batchInProgress = false;
                batchLock.notifyAll();
            }
        }
    }

    public boolean isStopGeneration() {
        return stopGeneration;
    }
    
    public void shutdown() {
        stopGeneration = true;
        isRunning = false;
        
        if (controllerThread != null) {
            controllerThread.interrupt();
        }
        
        synchronized (batchLock) {
            batchLock.notifyAll();
        }
    }

    public void restart() {
        stopGeneration = false;
        isRunning = true;
        
        startControllerThread();
        
        synchronized (batchLock) {
            activeWordCount = 0;
            batchInProgress = false;
            batchLock.notifyAll();
        }
    }
}