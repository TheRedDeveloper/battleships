import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** AI written, I let Claude mess with shitty Java. @author Claude */
public class AsciiDisplay {
    private static final int GRID_WIDTH  = 50;
    private static final int GRID_HEIGHT = 24;
    public static final int getGridWidth()  { return GRID_WIDTH;  }
    public static final int getGridHeight() { return GRID_HEIGHT; } 
        
    private char[][] displayBuffer;
    private JFrame frame;
    private JTextArea textArea;
    
    private InputManager inputManager;
    
    public AsciiDisplay(InputManager inputManager) {
        this.inputManager = inputManager;
        displayBuffer = new char[GRID_HEIGHT][GRID_WIDTH];
        
        clearBuffer();
        
        try {
            SwingUtilities.invokeAndWait(() -> {
                frame = new JFrame("ASCII Display");
                frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.out.println("Window closing...");
                        Game.stop();
                        frame.dispose();
                        System.exit(0);
                    }
                });
                
                textArea = new JTextArea(GRID_HEIGHT, GRID_WIDTH);
                textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
                textArea.setEditable(false);
                textArea.setForeground(Color.WHITE);
                textArea.setBackground(Color.BLACK);
                textArea.setFocusable(true);
                
                inputManager.initialize(textArea, GRID_WIDTH, GRID_HEIGHT);
                
                frame.add(new JScrollPane(textArea));
                
                frame.setSize(650, 600); // Increased window size
                frame.setLocationRelativeTo(null);
                frame.setResizable(false);
                
                System.out.println("AsciiDisplay frame initialized");
            });
        } catch (Exception e) {
            System.err.println("Error initializing AsciiDisplay: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void show() {
        System.out.println("Showing AsciiDisplay...");
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.setVisible(true);
                textArea.requestFocusInWindow(); // Ensure keyboard focus
                System.out.println("Frame should now be visible");
                refreshDisplay();
            } else {
                System.err.println("Cannot show display - frame is null");
            }
        });
    }
    
    public KeyEvent pollKeyEvent() {
        return inputManager.pollKeyEvent();
    }
    
    public boolean hasKeyEvents() {
        return inputManager.hasKeyEvents();
    }
    
    public int[] getMousePosition() {
        return inputManager.getMousePosition();
    }
    
    public boolean isMousePressed() {
        return inputManager.isMousePressed();
    }
    
    public boolean checkAndResetMouseClicked() {
        return inputManager.checkAndResetMouseClicked();
    }
    
    public boolean checkAndResetRightMouseClicked() {
        return inputManager.checkAndResetRightMouseClicked();
    }
    
    public final void clearBuffer() {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                displayBuffer[y][x] = ' ';
            }
        }
    }
    
    public void setCharacter(int x, int y, char c) {
        if (x >= 0 && x < GRID_WIDTH && y >= 0 && y < GRID_HEIGHT) {
            displayBuffer[y][x] = c;
        }
    }
    
    public void drawString(int x, int y, String text) {
        for (int i = 0; i < text.length(); i++) {
            if (x + i < GRID_WIDTH && y < GRID_HEIGHT) {
                setCharacter(x + i, y, text.charAt(i));
            }
        }
    }
    
    public void refreshDisplay() {
        if (textArea == null) {
            System.err.println("Cannot refresh - textArea is null");
            return;
        }
        
        final StringBuilder sb = new StringBuilder();
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                sb.append(displayBuffer[y][x]);
            }
            if (y < GRID_HEIGHT - 1) {
                sb.append('\n');
            }
        }
        
        final String content = sb.toString();
        SwingUtilities.invokeLater(() -> {
            textArea.setText(content);
            textArea.requestFocusInWindow();
        });
    }
}