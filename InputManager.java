import java.awt.Component;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Queue;

/** Handles keyboard and mouse input for the game interface. 
 * 
 * Manages input events and coordinates between pixel space and game grid space.
 * @author Claude */
public class InputManager {
    // Calibration factors for converting pixel coordinates to grid coordinates
    private float calibrationFactorX = 1/9f;
    private float calibrationFactorY = 1/19.04f;

    // Key event handling
    private final Queue<KeyEvent> keyEvents = new LinkedList<>();
    private final Object keyLock = new Object();
    
    // Mouse event handling
    private int rawX = 0;
    private int rawY = 0;
    private int mouseX = 0;
    private int mouseY = 0;
    private boolean mousePressed = false;
    private boolean mouseClicked = false;
    private boolean rightMouseClicked = false;
    private final Object mouseLock = new Object();
    
    /** Initialize input handlers for any component */
    public void initialize(Component component, int gridWidth, int gridHeight) {
        Game.LOGGER.info("Initializing InputManager with " + component.getClass().getSimpleName());
        
        // Add key listener to capture keyboard input
        component.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                synchronized (keyLock) {
                    keyEvents.add(e);
                    // Only log important keys like ESC, not every keystroke
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        Game.LOGGER.info("Key pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
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
            // Convert pixel coordinates to grid coordinates
            rawX = e.getX();
            rawY = e.getY();
            mouseX = (int)Math.floor(rawX * calibrationFactorX);
            mouseY = (int)Math.floor(rawY * calibrationFactorY);
            
            // Ensure coordinates are within bounds
            mouseX = Math.clamp(mouseX, 0, gridWidth - 1);
            mouseY = Math.clamp(mouseY, 0, gridHeight - 1);
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

    /** Calibrate by the cursor being at the center of Position[49, 23] (49.5 23.5) */
    public void calibrate() {
        // Calculate the calibration factors based on the given raw coordinates
        calibrationFactorX = 49.5f / rawX;
        calibrationFactorY = 23.5f / rawY;

        Game.calibrateDisplay((int) (rawX / 49.5), (int) (rawY / 23.5));
        
        // Log the calibration factors for debugging
        Game.LOGGER.info("Calibration input: rawX = " + rawX + ", rawY = " + rawY);
        Game.LOGGER.info("Calibration factors set: X = " + calibrationFactorX + ", Y = " + calibrationFactorY);
        
    }
}
