import java.awt.event.KeyEvent;

public class StartMenuMode extends GameMode {
    private StartMenuScreen screen = StartMenuScreen.MAIN;
    private int selectedOption = 0;

    private static StartMenuMode instance = null;
    
    public static StartMenuMode getInstance() {
        if (instance == null) {
            instance = new StartMenuMode();
        }
        return instance;
    }
    
    @Override
    public void render(GameState gameState, AsciiDisplay display) {
        display.clearBuffer();
        
        // Adjusted for wider screen (50 columns)
        int startY = 5; // Vertical position for menu
        int centerX = 20; // More centered horizontally
        
        switch (screen) {
            case MAIN:
                display.drawString(centerX, startY, "B A T T L E S H I P S");
                display.drawString(centerX, startY + 2, (selectedOption == 0 ? "> " : "  ") + "1. Start Game");
                display.drawString(centerX, startY + 3, (selectedOption == 1 ? "> " : "  ") + "2. Credits");
                display.drawString(centerX, startY + 4, (selectedOption == 2 ? "> " : "  ") + "3. Exit");
                display.drawString(centerX - 5, startY + 7, "Controls: Up/Down arrows or j/k, Enter to select");
                break;
            case CREDITS:
                display.drawString(centerX, startY, "C R E D I T S");
                display.drawString(centerX, startY + 2, "Developed by RedDev");
                display.drawString(centerX - 5, startY + 4, "Press any key to return to the main menu.");
                break;
        }
        
        display.refreshDisplay();
    }

    @Override
    public GameState update(GameState gameState, InputManager inputManager) {
        while (inputManager.hasKeyEvents()) {
            KeyEvent keyEvent = inputManager.pollKeyEvent();
            if (keyEvent != null) {
                processKeyEvent(keyEvent, gameState);
            }
        }
        
        return gameState;
    }
    
    /**
     * Process a key event and update the game state accordingly
     */
    private void processKeyEvent(KeyEvent e, GameState gameState) {
        int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        
        if (screen == StartMenuScreen.MAIN) {
            // Handle main menu key presses
            switch (keyCode) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_K:
                    // Move selection up
                    selectedOption = Math.max(0, selectedOption - 1);
                    break;
                    
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_J:
                    // Move selection down
                    selectedOption = Math.min(2, selectedOption + 1);
                    break;
                    
                case KeyEvent.VK_ENTER:
                    // Select current option
                    handleSelection(gameState);
                    break;
                    
                default:
                    // Handle numeric shortcuts
                    if (keyChar == '1') {
                        selectedOption = 0;
                        handleSelection(gameState);
                    } else if (keyChar == '2') {
                        selectedOption = 1;
                        handleSelection(gameState);
                    } else if (keyChar == '3') {
                        selectedOption = 2;
                        handleSelection(gameState);
                    }
                    break;
            }
        } else if (screen == StartMenuScreen.CREDITS) {
            // Any key press in credits returns to main menu
            screen = StartMenuScreen.MAIN;
        }
    }
    
    private void handleSelection(GameState gameState) {
        switch (selectedOption) {
            case 0:  // Start Game
                gameState.setMode(BuildMode.getInstance()); // Start with BuildMode to place ships
                break;
            case 1:  // Credits
                screen = StartMenuScreen.CREDITS;
                break;
            case 2:  // Exit
                Game.stop();
                break;
        }
    }
    
    @Override
    public void enter() {
        screen = StartMenuScreen.MAIN;
        selectedOption = 0;
    }

    @Override
    public void exit() {}
}