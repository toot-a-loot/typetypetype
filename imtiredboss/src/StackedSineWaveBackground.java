import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

public class StackedSineWaveBackground extends JPanel {
    private int WIDTH; // Width of the panel
    private int HEIGHT; // Height of the panel
    private static final int NUM_WAVES = 5; // Number of stacked sine waves
    private static final int SPEED = 100; // Speed of the animation (pixels per frame)
    private int WAVE_HEIGHT;
    private static final int STROKE_SIZE = 100; 
    private static final int PIXEL_SIZE = 4;
    private double phase; 
    
    private Color waveColor = new Color(80, 80, 80);
    
    private Color wavePulseColor = new Color(100, 100, 100);
    private Color backgroundPulseColor = new Color(60, 60, 60); 
    
    private Color backgroundColor = new Color(40, 40, 40); 
    
    private float pulseIntensity = 0.0f;
    
    private float pulseDecayFactor = 0.85f; 
    
    public StackedSineWaveBackground(int width, int height) {
        phase = 0;
        this.WIDTH = width;
        this.HEIGHT = height;
        this.WAVE_HEIGHT = HEIGHT / NUM_WAVES;
        // Set up the timer for animation
        Timer timer = new Timer(16, new ActionListener() { // ~60 FPS
            @Override
            public void actionPerformed(ActionEvent e) {
                phase += SPEED * 0.01; // Update phase for animation
                updatePulse(); // Update the pulse effect
                repaint(); // Redraw the sine waves and background
            }
        });
        timer.start(); // Start the animation
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable anti-aliasing for smoother squares
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Calculate the pulsed background color
        Color currentBackgroundColor = interpolateColors(backgroundColor, backgroundPulseColor, pulseIntensity);
        g2d.setColor(currentBackgroundColor);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Calculate the pulsed wave color
        Color currentWaveColor = interpolateColors(waveColor, wavePulseColor, pulseIntensity);
        g2d.setColor(currentWaveColor);

        // Draw each pixelated sine wave
        for (int i = 0; i < NUM_WAVES; i++) {
            // Calculate the y offset for stacking
            int yOffset = i * WAVE_HEIGHT;
            drawThickPixelatedSineWave(g2d, phase, yOffset);
        }
    }

    /**
     * Interpolate between two colors based on a factor (0.0 to 1.0)
     */
    private Color interpolateColors(Color c1, Color c2, float factor) {
        int r = (int) (c1.getRed() + factor * (c2.getRed() - c1.getRed()));
        int g = (int) (c1.getGreen() + factor * (c2.getGreen() - c1.getGreen()));
        int b = (int) (c1.getBlue() + factor * (c2.getBlue() - c1.getBlue()));
        
        // Ensure RGB values stay within valid range
        r = Math.min(255, Math.max(0, r));
        g = Math.min(255, Math.max(0, g));
        b = Math.min(255, Math.max(0, b));
        
        return new Color(r, g, b);
    }

    private void drawThickPixelatedSineWave(Graphics2D g2d, double phase, int yOffset) {
        // Calculate number of horizontal pixels
        int numHorizontalPixels = WIDTH / PIXEL_SIZE;
        
        // Draw multiple rows of pixels to create thickness
        int halfStroke = STROKE_SIZE / 2;
        
        // Draw "pixels" for the sine wave with thickness
        for (int i = 0; i < numHorizontalPixels; i++) {
            double x = i * PIXEL_SIZE;
            
            // Calculate center y position based on sine function
            double centerY = yOffset + WAVE_HEIGHT / 2 + 
                             50 * Math.sin(2 * Math.PI * (x + phase) / (WIDTH / 4));
            
            // Snap to pixel grid (round to nearest PIXEL_SIZE multiple)
            double pixelatedCenterY = Math.round(centerY / PIXEL_SIZE) * PIXEL_SIZE;
            
            // Draw a column of pixels to create the thick stroke
            for (int j = -halfStroke / PIXEL_SIZE; j <= halfStroke / PIXEL_SIZE; j++) {
                double pixelY = pixelatedCenterY + j * PIXEL_SIZE;
                
                // Draw the pixel as a filled square
                g2d.fill(new Rectangle2D.Double(x, pixelY, PIXEL_SIZE, PIXEL_SIZE));
            }
        }
    }

    private void updatePulse() {
        // Gradually reduce the pulse effect with faster decay
        pulseIntensity = Math.max(0.0f, pulseIntensity * pulseDecayFactor);
    }

    // Method to set the wave and pulse colors
    public void setColors(Color waveColor, Color wavePulseColor, Color backgroundColor, Color backgroundPulseColor) {
        this.waveColor = waveColor;
        this.wavePulseColor = wavePulseColor;
        this.backgroundColor = backgroundColor;
        this.backgroundPulseColor = backgroundPulseColor;
    }

    public void triggerPulse() {
        pulseIntensity = 1.0f; // Reset the pulse intensity to trigger the animation
    }
}