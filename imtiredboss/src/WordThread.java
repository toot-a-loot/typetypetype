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

    private static volatile int activeWordCount = 0;
    private static volatile boolean batchInProgress = false;
    private static final Object batchLock = new Object();

    private static final int MIN_WORD_SPAWN_DELAY = 200;
    private static final int MAX_WORD_SPAWN_DELAY = 600;

    private static final ExecutorService wordAnimationPool = Executors.newFixedThreadPool(8);

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
                    waitForWordsClear();
                    
                    if (stopGeneration || !isRunning) break;

                    spawnWordBatch();

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
            while ((activeWordCount > 0 || batchInProgress) && isRunning) {
                if (stopGeneration) return;
                batchLock.wait(100);
            }
        }
    }
    
    private void spawnWordBatch() {
        synchronized (batchLock) {
            batchInProgress = true;
            activeWordCount = MAX_WORDS;
        }

        for (int i = 0; i < MAX_WORDS; i++) {
            final int threadId = i;

            new Thread(() -> {
                try {
                    if (threadId > 0) {
                        int delay = MIN_WORD_SPAWN_DELAY + random.nextInt(MAX_WORD_SPAWN_DELAY - MIN_WORD_SPAWN_DELAY + 1);
                        Thread.sleep(delay * threadId);
                    }

                    if (stopGeneration || !isRunning) {
                        decrementWordSafely();
                        return;
                    }

                    String word = wordGenerator.getRandomWord(wordLength);
                    if (word != null && !stopGeneration && isRunning) {
                        animateWord(word);
                    } else {
                        decrementWordSafely();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    decrementWordSafely();
                }
            }).start();
        }
    }

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

        FontMetrics metrics = wordLabel.getFontMetrics(wordLabel.getFont());
        int textWidth = metrics.stringWidth(word);

        int labelWidth = textWidth + 10;
        int labelHeight = 30;

        int screenWidth = 720;
        int padding = 60;
        int minX = padding;
        int maxX = screenWidth - padding - labelWidth;
        int xPosition = minX + random.nextInt(maxX - minX + 1);

        int initialY = 20;
        wordLabel.setBounds(xPosition, initialY, labelWidth, labelHeight);

        SwingUtilities.invokeLater(() -> {
            if (!stopGeneration && isRunning) {
                play.addWordLabel(wordLabel);
            } else {
                decrementWordSafely();
                return;
            }
        });

        wordAnimationPool.execute(() -> {
            final boolean[] wordRemoved = {false};

            int yPos = initialY;
            
            while (yPos < 840 && !stopGeneration && isRunning) {
                final int currentY = yPos;

                boolean stillPresent = false;
                try {
                    final boolean[] checkResult = {false};
                    SwingUtilities.invokeAndWait(() -> {
                        checkResult[0] = play.containsWordLabel(wordLabel);
                        if (checkResult[0]) {
                            wordLabel.setLocation(wordLabel.getX(), currentY);
                        }
                    });
                    stillPresent = checkResult[0];
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (!stillPresent) {
                    if (!wordRemoved[0]) {
                        wordRemoved[0] = true;
                    }
                    return;
                }

                yPos++;
                
                try {
                    Thread.sleep(6);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            final int finalY = yPos;
            SwingUtilities.invokeLater(() -> {
                if (finalY >= 840 && play.containsWordLabel(wordLabel) && isRunning) {
                    stopGeneration = true;

                    synchronized (batchLock) {
                        batchLock.notifyAll();
                    }
                    
                    play.showGameOverMessage();
                }

                if (play.containsWordLabel(wordLabel)) {
                    wordRemoved[0] = true;
                    play.removeWordLabel(wordLabel);
                    decrementWordSafely();
                }
            });
        });
    }

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