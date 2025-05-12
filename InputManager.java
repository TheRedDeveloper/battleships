import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;

/** Handles keyboard and mouse input for the game interface. 
 * 
 * Manages input events and coordinates between pixel space and game grid space.
 * @author Claude */
public class InputManager {
    // Key event handling
    private final Queue<KeyEvent> keyEvents = new LinkedList<>();
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

    /** Initialize input handlers for any component */
    public void initialize(Component component, int gridWidth, int gridHeight) {
        Game.LOGGER.log(Level.INFO, "Initializing InputManager with " + component.getClass().getSimpleName());
        
        // Add key listener to capture keyboard input
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (keyLock) {
                    keyEvents.add(e);
                    // Only log important keys like ESC, not every keystroke
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        Game.LOGGER.log(Level.INFO, "Key pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
                    }
                }
            }
        });
        
        // Add mouse listeners for mouse input
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                synchronized (mouseLock) {
                    mousePressed = true;
                    updateMousePosition(e, component, gridWidth, gridHeight);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                synchronized (mouseLock) {
                    mousePressed = false;
                    updateMousePosition(e, component, gridWidth, gridHeight);
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
                    updateMousePosition(e, component, gridWidth, gridHeight);
                }
            }
        });
        
        component.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateMousePosition(e, component, gridWidth, gridHeight);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                synchronized (mouseLock) {
                    mousePressed = true;
                    updateMousePosition(e, component, gridWidth, gridHeight);
                }
            }
        });
    }

    private void updateMousePosition(MouseEvent e, Component component, int gridWidth, int gridHeight) {
        synchronized (mouseLock) {
            // Calculate character dimensions if not set
            if (charWidth == 0 || charHeight == 0) {
                FontMetrics metrics = component.getFontMetrics(component.getFont());
                charWidth = metrics.charWidth('W'); // 10
                charHeight = metrics.getHeight(); // 14
                
                Game.LOGGER.log(Level.INFO, "Character dimensions: width=" + charWidth + ", height=" + charHeight);
            }
            
            // Log raw mouse position and calculations
            float rawX = e.getX();
            float rawY = e.getY();
            
            // Convert pixel coordinates to grid coordinates
            // For X: each cell is approximately 0.9 * charWidth pixels
            // For Y: each cell is approximately 1.36 * charHeight pixels
            mouseX = (int)Math.floor(rawX / (charWidth * 0.9));
            mouseY = (int)Math.floor(rawY / (charHeight * 1.36));
            
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
     *  @return Position with x, y coordinates */
    public Position getMousePosition() {
        synchronized (mouseLock) {
            return new Position(mouseX, mouseY);
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
