import java.awt.FontMetrics;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.JTextArea;

/** AI written, so blame Claude if not working. @author Claude */
public class InputManager {
    // Key event handling
    private Queue<KeyEvent> keyEvents = new LinkedList<>();
    private final Object keyLock = new Object();
    
    // Mouse event handling
    private int mouseX = 0;
    private int mouseY = 0;
    private boolean mousePressed = false;
    private boolean mouseClicked = false;
    private boolean rightMouseClicked = false;
    private final Object mouseLock = new Object();
    
    // Character dimensions for coordinate conversion
    private float charWidth = 0;
    private float charHeight = 0;

    /** Initialize input handlers for the provided text area */
    public void initialize(JTextArea textArea, int gridWidth, int gridHeight) {
        // Add key listener to capture keyboard input
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (keyLock) {
                    keyEvents.add(e);
                }
            }
        });
        
        // Add mouse listeners for mouse input
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                synchronized (mouseLock) {
                    mousePressed = true;
                    updateMousePosition(e, textArea, gridWidth, gridHeight);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                synchronized (mouseLock) {
                    mousePressed = false;
                    updateMousePosition(e, textArea, gridWidth, gridHeight);
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                synchronized (mouseLock) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        mouseClicked = true;
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        rightMouseClicked = true;
                    }
                    updateMousePosition(e, textArea, gridWidth, gridHeight);
                }
            }
        });
        
        textArea.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateMousePosition(e, textArea, gridWidth, gridHeight);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                synchronized (mouseLock) {
                    mousePressed = true;
                    updateMousePosition(e, textArea, gridWidth, gridHeight);
                }
            }
        });
    }
    
    private void updateMousePosition(MouseEvent e, JTextArea textArea, int gridWidth, int gridHeight) {
        synchronized (mouseLock) {
            // Calculate character dimensions if not set
            if (charWidth == 0 || charHeight == 0) {
                FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
                charWidth = metrics.charWidth('M'); // Use 'M' as a reference for monospace width
                charHeight = metrics.getHeight();
            }
            
            // Convert pixel coordinates to grid coordinates
            mouseX = (int)(e.getX() / charWidth);
            mouseY = (int)(e.getY() / charHeight);
            
            // Ensure coordinates are within bounds
            mouseX = Math.max(0, Math.min(gridWidth - 1, mouseX));
            mouseY = Math.max(0, Math.min(gridHeight - 1, mouseY));
        }
    }
    
    /** Polls for the next key event, removing it from the queue. 
     *  @return The next KeyEvent, or null if no events are available */
    public KeyEvent pollKeyEvent() {
        synchronized (keyLock) {
            return keyEvents.isEmpty() ? null : keyEvents.poll();
        }
    }
    
    /** Checks if any key events are available.
     *  @return true if there are key events to process */
    public boolean hasKeyEvents() {
        synchronized (keyLock) {
            return !keyEvents.isEmpty();
        }
    }
    
    /** Gets the current mouse position in grid coordinates 
     *  @return int[2] with x, y coordinates */
    public int[] getMousePosition() {
        synchronized (mouseLock) {
            return new int[]{mouseX, mouseY};
        }
    }
    
    /** Checks if mouse is being pressed */
    public boolean isMousePressed() {
        synchronized (mouseLock) {
            return mousePressed;
        }
    }
    
    /** Checks if mouse was clicked and resets the flag */
    public boolean checkAndResetMouseClicked() {
        synchronized (mouseLock) {
            boolean result = mouseClicked;
            mouseClicked = false;
            return result;
        }
    }
    
    /** Checks if right mouse was clicked and resets the flag */
    public boolean checkAndResetRightMouseClicked() {
        synchronized (mouseLock) {
            boolean result = rightMouseClicked;
            rightMouseClicked = false;
            return result;
        }
    }
}
