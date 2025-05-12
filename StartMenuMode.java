import java.awt.event.KeyEvent;

/** @author Claude */
public class StartMenuMode extends GameMode {
    private static final String[] MENU_OPTIONS = {
        "Start Game",
        "Credits",
        "Exit"
    };
    
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
        switch (screen) {
            case MAIN:
                
                display.clearBuffer();
                
                // Draw the game title with cyan color
                display.drawString(10, 2, "B A T T L E S H I P S", ANSI.BRIGHT_CYAN);
                
                // Draw the menu options with different colors based on selection
                for (int i = 0; i < MENU_OPTIONS.length; i++) {
                    if (i == selectedOption) {
                        // Selected option gets bright white on blue background
                        display.drawString(10, 5 + i, "> " + MENU_OPTIONS[i], ANSI.BRIGHT_WHITE, ANSI.BLUE);
                    } else {
                        // Other options get white text
                        display.drawString(10, 5 + i, "  " + MENU_OPTIONS[i], ANSI.WHITE);
                    }
                }
                
                // Draw some water at the bottom as decoration
                for (int y = 15; y < 20; y++) {
                    for (int x = 0; x < 50; x++) {
                        // Alternate between regular and bright blue for a wavy effect
                        String waveColor = (x + y) % 2 == 0 ? ANSI.BLUE : ANSI.BRIGHT_BLUE;
                        display.setCharacter(x, y, '~', waveColor);
                    }
                }
                
                // Draw a small ship on the water
                display.drawString(30, 13, ".", ANSI.BRIGHT_WHITE);
                display.drawString(29, 14, "/|\\", ANSI.BRIGHT_WHITE);
                display.drawString(28, 15, "/_|_\\", ANSI.BRIGHT_WHITE);
                display.drawString(33, 15, "_", ANSI.YELLOW);
                display.drawString(29, 16, "\\___/", ANSI.YELLOW);
                
                display.drawString(5, 16, "~~~><>", ANSI.BRIGHT_CYAN);
                display.drawString(40, 18, "><((°>", ANSI.BRIGHT_GREEN);
                display.drawString(15, 19, "><>", ANSI.BRIGHT_MAGENTA);
                
                display.refreshDisplay();
                break;
            case CREDITS:
                display.clearBuffer();
                
                display.refreshDisplay();
                break;
            default:
                throw new AssertionError();
        }
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
        
        if (screen == StartMenuScreen.MAIN) {
            switch (keyCode) {
                case KeyEvent.VK_UP, KeyEvent.VK_K:
                    // Move selection up
                    selectedOption = Math.max(0, selectedOption - 1);
                    break;
                    
                case KeyEvent.VK_DOWN, KeyEvent.VK_J:
                    // Move selection down
                    selectedOption = Math.min(2, selectedOption + 1);
                    break;
                    
                case KeyEvent.VK_ENTER:
                    // Select current option
                    handleSelection(gameState);
                    break;
                    
                default:
                    char keyChar = e.getKeyChar();
                    switch (keyChar) {
                        case '1':
                            selectedOption = 0;
                            handleSelection(gameState);
                            break;
                        case '2':
                            selectedOption = 1;
                            handleSelection(gameState);
                            break;
                        case '3':
                            selectedOption = 2;
                            handleSelection(gameState);
                            break;
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