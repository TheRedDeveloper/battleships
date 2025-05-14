import java.awt.*;
import java.awt.event.*;
import java.util.logging.Level;
import javax.swing.*;

/** ASCII-based display for rendering game content with color support.
 * 
 * Uses direct rendering on a JPanel with character grid buffer for text and colors.
 * 
 * @author Claude */
public class AsciiDisplay {
    private static final int GRID_WIDTH = 50;
    private static final int GRID_HEIGHT = 24;
    public static final int getGridWidth() { return GRID_WIDTH; }
    public static final int getGridHeight() { return GRID_HEIGHT; }
    
    private char[][] displayBuffer;
    private Color[][] colorBuffer;
    private Color[][] backgroundBuffer;
    private JFrame frame;
    private DisplayPanel panel;
    private InputManager inputManager;

    private int calibrationWidth = 9;
    private int calibrationHeight = 19;
    
    /** Panel that renders the character grid with colors */
    private class DisplayPanel extends JPanel {
        private Font font;
        private int charWidth;
        private int charHeight;
        
        public DisplayPanel() {
            // Try to use Noto Mono with fallbacks to other monospace fonts
            String[] fontNames = {"DejaVu Sans Mono", Font.MONOSPACED};
            font = null;
            
            // Try each font in order of preference
            for (String fontName : fontNames) {
                try {
                    font = new Font(fontName, Font.PLAIN, 16);
                    // Test if this font exists in the system
                    if (!font.getFamily().equals(fontName) && !fontName.equals(Font.MONOSPACED)) {
                        continue; // Try the next font if this one isn't available
                    }
                    Game.LOGGER.log(Level.INFO, "Using font: " + fontName);
                    break;
                } catch (Exception e) {
                    // Continue to next font if this one fails
                    continue;
                }
            }
            
            // If all preferred fonts failed, fall back to default monospaced
            if (font == null) {
                font = new Font(Font.MONOSPACED, Font.PLAIN, 16);
                Game.LOGGER.log(Level.INFO, "Using default monospaced font");
            }
            
            setBackground(Color.BLACK);
            setForeground(Color.WHITE);
            
            // Estimate character dimensions initially
            FontMetrics metrics = getFontMetrics(font);
            charWidth = metrics.charWidth('W');
            charHeight = metrics.getHeight();

            // Set preferred size based on character dimensions
            setPreferredSize(new Dimension(GRID_WIDTH * calibrationWidth, GRID_HEIGHT * calibrationHeight));
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            // Use better text rendering
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(font);
            
            FontMetrics metrics = g2d.getFontMetrics();
            charWidth = metrics.charWidth('W');
            charHeight = metrics.getHeight();
            
            // Draw each character with its colors
            for (int y = 0; y < GRID_HEIGHT; y++) {
                for (int x = 0; x < GRID_WIDTH; x++) {
                    int pixelX = x * charWidth;
                    int pixelY = y * charHeight;
                    
                    // Draw background
                    g2d.setColor(backgroundBuffer[y][x]);
                    g2d.fillRect(pixelX, pixelY, charWidth, charHeight);
                    
                    // Draw character
                    g2d.setColor(colorBuffer[y][x]);
                    g2d.drawString(String.valueOf(displayBuffer[y][x]), 
                                  pixelX, pixelY + metrics.getAscent());
                }
            }
        }
    }
    
    public AsciiDisplay(InputManager inputManager) {
        this.inputManager = inputManager;
        displayBuffer = new char[GRID_HEIGHT][GRID_WIDTH];
        colorBuffer = new Color[GRID_HEIGHT][GRID_WIDTH];
        backgroundBuffer = new Color[GRID_HEIGHT][GRID_WIDTH];
        
        clearBuffer();
        
        try {
            SwingUtilities.invokeAndWait(() -> {
                frame = new JFrame("ASCII Display");
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        Game.LOGGER.log(Level.INFO, "Window closing event received");
                        Game.stop();
                        frame.dispose();
                        System.exit(0);
                    }
                });
                
                panel = new DisplayPanel();
                
                // Add key listener for ESC key
                KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
                Action escapeAction = new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Game.LOGGER.log(Level.INFO, "ESC key pressed, closing window...");
                        Game.stop();
                        frame.dispose();
                        System.exit(0);
                    }
                };
                
                panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
                panel.getActionMap().put("ESCAPE", escapeAction);
                
                inputManager.initialize(panel, GRID_WIDTH, GRID_HEIGHT);
                
                frame.add(panel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);
                
                Game.LOGGER.log(Level.INFO, "AsciiDisplay frame initialized");
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Game.LOGGER.log(Level.SEVERE, "Thread interrupted while initializing AsciiDisplay: " + e.getMessage());
        } catch (java.lang.reflect.InvocationTargetException e) {
            Game.LOGGER.log(Level.SEVERE, "Error initializing AsciiDisplay: " + e.getMessage());
        }
    }
    
    public void show() {
        Game.LOGGER.log(Level.INFO, "Showing AsciiDisplay...");
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.setVisible(true);
                panel.requestFocusInWindow();
                Game.LOGGER.log(Level.INFO, "Frame should now be visible");
                refreshDisplay();
            } else {
                Game.LOGGER.log(Level.SEVERE, "Cannot show display - frame is null");
            }
        });
    }
    
    public final void clearBuffer() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                displayBuffer[y][x] = ' ';
                colorBuffer[y][x] = Color.WHITE;
                backgroundBuffer[y][x] = Color.BLACK;
            }
        }
    }

    /**
     * Resizes the display panel to the specified dimensions
     */
    public void resize(int width, int height) {
        if (panel != null) {
            panel.setPreferredSize(new Dimension(width, height));
            panel.setSize(new Dimension(width, height));
            frame.pack();
        }
    }

    /**
     * Calibrates the display panel with new character width and height values
     */
    public void calibrate(int calibrationX, int calibrationY) {
        if (panel != null) {
            calibrationWidth = calibrationX + 1;
            resize(GRID_WIDTH * calibrationWidth, GRID_HEIGHT * calibrationHeight);
        }
    }
    
    public void setCharacter(int x, int y, char c) {
        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            displayBuffer[y][x] = c;
        }
    }

    public void setCharacter(int x, int y, char c, Color textColor) {
        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            displayBuffer[y][x] = c;
            colorBuffer[y][x] = textColor;
        }
    }
    
    public void setCharacter(int x, int y, char c, Color textColor, Color backgroundColor) {
        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            displayBuffer[y][x] = c;
            colorBuffer[y][x] = textColor;
            backgroundBuffer[y][x] = backgroundColor;
        }
    }
    
    public void drawString(int x, int y, String text) {
        for (int i = 0; i < text.length(); i++) {
            if (x + i >= 0 && x + i < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                setCharacter(x + i, y, text.charAt(i));
            }
        }
    }
    
    public void drawString(int x, int y, String text, Color textColor) {
        for (int i = 0; i < text.length(); i++) {
            if (x + i >= 0 && x + i < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                setCharacter(x + i, y, text.charAt(i), textColor);
            }
        }
    }
    
    public void drawString(int x, int y, String text, Color textColor, Color backgroundColor) {
        for (int i = 0; i < text.length(); i++) {
            if (x + i >= 0 && x + i < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                setCharacter(x + i, y, text.charAt(i), textColor, backgroundColor);
            }
        }
    }
    
    /** 
     * Draws a string without overwriting existing characters with spaces.
     * Only non-whitespace characters in the string will be drawn.
     */
    public void drawTransparentString(int x, int y, String text) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ' && x + i >= 0 && x + i < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                setCharacter(x + i, y, c);
            }
        }
    }
    
    /** 
     * Draws a string with the specified text color without overwriting existing
     * characters with spaces. Only non-whitespace characters in the string will be drawn.
     */
    public void drawTransparentString(int x, int y, String text, Color textColor) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ' && x + i >= 0 && x + i < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                setCharacter(x + i, y, c, textColor);
            }
        }
    }
    
    /** 
     * Draws a string with the specified text and background colors without overwriting
     * existing characters with spaces. Only non-whitespace characters in the string will be drawn.
     */
    public void drawTransparentString(int x, int y, String text, Color textColor, Color backgroundColor) {
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c != ' ' && x + i >= 0 && x + i < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
                setCharacter(x + i, y, c, textColor, backgroundColor);
            }
        }
    }
    
    /**
     * Draws a string with the specified ANSI color without overwriting existing
     * characters with spaces. Only non-whitespace characters in the string will be drawn.
     */
    public void drawTransparentString(int x, int y, String text, String ansiColor) {
        drawTransparentString(x, y, text, translateAnsiColor(ansiColor));
    }
    
    /**
     * Draws a string with the specified ANSI foreground and background colors without
     * overwriting existing characters with spaces. Only non-whitespace characters will be drawn.
     */
    public void drawTransparentString(int x, int y, String text, String foregroundColor, String backgroundColor) {
        drawTransparentString(x, y, text, translateAnsiColor(foregroundColor), translateAnsiColor(backgroundColor));
    }
    
    public void refreshDisplay() {
        if (panel == null) {
            Game.LOGGER.log(Level.SEVERE, "Cannot refresh - display panel is null");
            return;
        }

        // Repaint the panel to show the updated buffer
        SwingUtilities.invokeLater(() -> {
            panel.repaint();
        });
    }

    // Color helper methods to integrate with existing ANSI class
    
    public void drawString(int x, int y, String text, String ansiColor) {
        drawString(x, y, text, translateAnsiColor(ansiColor));
    }
    
    public void drawString(int x, int y, String text, String foregroundColor, String backgroundColor) {
        drawString(x, y, text, translateAnsiColor(foregroundColor), translateAnsiColor(backgroundColor));
    }
    
    public void setCharacter(int x, int y, char c, String ansiColor) {
        setCharacter(x, y, c, translateAnsiColor(ansiColor));
    }
    
    public void setCharacter(int x, int y, char c, String foregroundColor, String backgroundColor) {
        setCharacter(x, y, c, translateAnsiColor(foregroundColor), translateAnsiColor(backgroundColor));
    }
    
    private Color translateAnsiColor(String ansiCode) {
        return switch (ansiCode) {
            case ANSI.BLACK -> Color.BLACK;
            case ANSI.RED -> Color.RED;
            case ANSI.GREEN -> Color.GREEN;
            case ANSI.YELLOW -> Color.YELLOW;
            case ANSI.BLUE -> Color.BLUE;
            case ANSI.MAGENTA -> Color.MAGENTA;
            case ANSI.CYAN -> Color.CYAN;
            case ANSI.WHITE -> Color.WHITE;
            case ANSI.BRIGHT_BLACK -> Color.DARK_GRAY;
            case ANSI.BRIGHT_RED -> new Color(255, 100, 100);
            case ANSI.BRIGHT_GREEN -> new Color(100, 255, 100);
            case ANSI.BRIGHT_YELLOW -> new Color(255, 255, 100);
            case ANSI.BRIGHT_BLUE -> new Color(100, 100, 255);
            case ANSI.BRIGHT_MAGENTA -> new Color(255, 100, 255);
            case ANSI.BRIGHT_CYAN -> new Color(100, 255, 255);
            case ANSI.BRIGHT_WHITE -> Color.WHITE;
            default -> Color.WHITE;
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                sb.append(displayBuffer[y][x]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}