import java.awt.event.KeyEvent;
import java.time.Duration;

/** Represents the main menu screen of the Battleships game.
 * 
 * This class handles the rendering and interaction with the main menu,
 * including options selection and the credits screen animation.
 * Main menu allows users to start a game, view credits, or exit.
 * 
 * @author Claude */
public class StartMenuMode extends GameMode {
    // Constants and member variables
    private static final String[] MENU_OPTIONS = {
        "Calibrate",
        "Easy",
        "Normal",
        "Hard",
        "Impossible",
        "Credits",
        "Exit"
    };
    
    // Credits screen variables
    private long creditsStartTime = 0;
    private static final long CREDITS_DURATION_MS = 40000;
    
    // Credits screen content
    private static final String[] CREDITS_TEXT = {
        "BATTLESHIPS",
        "",
        "Created by",
        "RedDev",
        "",
        "NullPointerException: Credits.java:42",
        "...just kidding, that was a joke",
        "",
        "",
        "",
        "",
        "Write Once, Debug Everywhere",
        "",
        "Saying Java is good because it runs anywhere",
        "is like saying anal sex is good because it",
        "works on all genders",
        "",
        "Why do Java developers wear glasses?",
        "Because they can't C#!",
        "",
        "The 'S' in Java stands for Speed",
        "",
        "My code runs on coffee. Java coffee.",
        "",
        "I had a problem so I decided to use Java",
        "Now I have a ProblemFactory",
        "",
        "I wanted to lean Java,",
        "but I couldn't find the class",
        "",
        "THANK YOU FOR PLAYING!",
        "",
        "Press any key to return"
    };
    
    // Animation parameters
    private static final int DISPLAY_HEIGHT = AsciiDisplay.getGridHeight();
    private static final int DISPLAY_WIDTH = AsciiDisplay.getGridWidth();
    
    // Star background
    private static class Star {
        int x;
        int y;
        char symbol;
        String color;
        double twinkleSpeed;
        
        Star(int x, int y, char symbol, String color, double speed) {
            this.x = x;
            this.y = y;
            this.symbol = symbol;
            this.color = color;
            this.twinkleSpeed = speed;
        }
    }
    
    private static final int STAR_COUNT = 32;
    private Star[] stars = new Star[STAR_COUNT];
    private static final char[] STAR_SYMBOLS = {'*', '.', '·', '-', '+'};
    private static final String[] STAR_COLORS = {
        ANSI.BRIGHT_WHITE, ANSI.WHITE, ANSI.BRIGHT_CYAN, ANSI.BRIGHT_YELLOW
    };
    
    // Ship animation
    private static class AnimatedShip {
        int x;
        int y;
        double speed;
        String[] art;
        String color;
        
        AnimatedShip(int y, double speed, String[] art, String color) {
            this.x = -10;
            this.y = y;
            this.speed = speed;
            this.art = art;
            this.color = color;
        }
    }
    
    private AnimatedShip[] ships = new AnimatedShip[2];
    
    private StartMenuScreen screen = StartMenuScreen.MAIN;
    private int selectedOption = 0;

    private static StartMenuMode instance = null;
    
    // Singleton pattern implementation
    public static StartMenuMode getInstance() {
        if (instance == null) {
            instance = new StartMenuMode();
        }
        return instance;
    }
    
    @Override
    public GameState enter(GameState gameState) {
        screen = StartMenuScreen.MAIN;
        selectedOption = 0;
        
        // Initialize stars for both menu and credits screens
        initializeStars();
        
        // Initialize ships for credits screen
        initializeShips();

        return gameState;
    }

    @Override
    public GameState update(GameState gameState, InputManager inputManager, Duration deltaTime) {
        while (inputManager.hasKeyEvents()) {
            KeyEvent keyEvent = inputManager.pollKeyEvent();
            if (keyEvent != null) {
                processKeyEvent(keyEvent, gameState);
            }
        }
        if (inputManager.checkAndResetMouseClicked() && screen == StartMenuScreen.CALIBRATE) {
            inputManager.calibrate();
            screen = StartMenuScreen.MAIN;
        }
        
        return gameState;
    }
    
    @Override
    public GameState exit(GameState gameState) { return gameState; }
    
    /**
     * Process a key event and update the game state accordingly
     */
    private void processKeyEvent(KeyEvent e, GameState gameState) {
        int keyCode = e.getKeyCode();
        
        if (screen == StartMenuScreen.MAIN) {
            switch (keyCode) {
                case KeyEvent.VK_UP, KeyEvent.VK_K -> selectedOption = Math.max(0, selectedOption - 1);
                case KeyEvent.VK_DOWN, KeyEvent.VK_J -> selectedOption = Math.min(6, selectedOption + 1);
                case KeyEvent.VK_ENTER -> handleSelection(gameState);
                default -> {
                    char keyChar = e.getKeyChar();
                    switch (keyChar) {
                        case '1' -> {
                            selectedOption = 0;
                            handleSelection(gameState);
                        }
                        case '2' -> {
                            selectedOption = 1;
                            handleSelection(gameState);
                        }
                        case '3' -> {
                            selectedOption = 2;
                            handleSelection(gameState);
                        }
                        case '4' -> {
                            selectedOption = 3;
                            handleSelection(gameState);
                        }
                        case '5' -> {
                            selectedOption = 4;
                            handleSelection(gameState);
                        }
                        case '6' -> {
                            selectedOption = 5;
                            handleSelection(gameState);
                        }
                        case '7' -> {
                            selectedOption = 6;
                            handleSelection(gameState);
                        }
                        default -> {
                            // Ignore other keys
                        }
                    }
                }
            }
        } else if (screen == StartMenuScreen.CREDITS) {
            // Any key press in credits returns to main menu
            screen = StartMenuScreen.MAIN;
        }
    }
    
    private void handleSelection(GameState gameState) {
        switch (selectedOption) {
            case 0 -> {  // Calibrate
                screen = StartMenuScreen.CALIBRATE;
                Game.resizeDisplay(10000, 10000);
            }
            case 1,2,3,4 -> { // Easy

                AttackStrategy attackStrategy = switch (selectedOption) {
                    case 1 -> new RandomAttackStrategy(); // Easy
                    case 2 -> new HuntAndTargetAttackStrategy(); // Normal
                    case 3 -> new ProbabilityAttackStrategy(); // Hard
                    case 4 -> new CheatAttackStrategy(); // Impossible
                    default -> throw new IllegalStateException("Unexpected value: " + selectedOption);
                };
                gameState.set(new GameState(BuildMode.getInstance(), new BotStrategy(new RandomGridStrategy(), attackStrategy)));
                gameState.set(BuildMode.getInstance().enter(gameState));
            }
            case 5 -> {  // Credits
                screen = StartMenuScreen.CREDITS;
                creditsStartTime = Game.getTimeSinceStart().toMillis();
            }
            case 6 -> Game.stop();
            default -> throw new IllegalStateException("Weird option selected: " + selectedOption);
        }
    }
    
    /**
     * Initialize stars with random positions for background effects
     */
    private void initializeStars() {
        // Initialize stars with random positions
        for (int i = 0; i < STAR_COUNT; i++) {
            int x = Game.RANDOM.nextInt(DISPLAY_WIDTH);
            int y = Game.RANDOM.nextInt(DISPLAY_HEIGHT);
            char symbol = STAR_SYMBOLS[Game.RANDOM.nextInt(STAR_SYMBOLS.length)];
            String color = STAR_COLORS[Game.RANDOM.nextInt(STAR_COLORS.length)];
            double twinkleSpeed = 0.5 + Game.RANDOM.nextDouble(); // Random speed between 0.5 and 1.5
            stars[i] = new Star(x, y, symbol, color, twinkleSpeed);
        }
    }
    
    /**
     * Initialize ships for credits animation
     */
    private void initializeShips() {
        // Initialize ships
        String[] shipArt1 = {
            "   .",
            "   |\\",
            " __|_\\____",
            "\\________/",
        };
        
        String[] shipArt2 = {
            "    .",
            "   /|\\",
            "  /_|_\\__",
            "  \\_____/"
        };
        
        ships[0] = new AnimatedShip(16, 0.8, shipArt1, ANSI.BRIGHT_YELLOW);
        ships[1] = new AnimatedShip(16, 0.5, shipArt2, ANSI.BRIGHT_RED);
    }
    
    @Override
    public void render(GameState gameState, AsciiDisplay display) {
        switch (screen) {
            case MAIN -> renderMainMenu(display);
            case CREDITS -> renderCreditsScreen(display);
            case CALIBRATE -> renderCalibrationScreen(display);
            default -> throw new AssertionError();
        }
    }
    
    /**
     * Renders the main menu screen with options and decorations
     */
    private void renderMainMenu(AsciiDisplay display) {
        display.clearBuffer();
        
        renderStarryBackground(display);
        renderMenuTitle(display);
        renderMenuOptions(display);
        renderWaterDecoration(display);
        
        display.refreshDisplay();
    }
    
    /**
     * Renders the credits screen with scrolling text and animations
     */
    private void renderCreditsScreen(AsciiDisplay display) {
        display.clearBuffer();
        
        // Calculate elapsed time and progress
        long currentTime = Game.getTimeSinceStart().toMillis();
        long elapsedTime = currentTime - creditsStartTime;
        double progress = (double) elapsedTime / CREDITS_DURATION_MS;
        
        // Auto-return to main menu after credits duration
        if (elapsedTime > CREDITS_DURATION_MS) {
            screen = StartMenuScreen.MAIN;
            display.refreshDisplay();
            return;
        }
        
        renderStarryBackground(display);
        renderAnimatedShips(display, currentTime);
        renderWaterWaves(display, currentTime);
        renderScrollingCredits(display, currentTime, progress);
        
        // Show time remaining
        int timeLeft = (int)((CREDITS_DURATION_MS - elapsedTime) / 1000) + 1;
        display.drawString(2, 1, "Credits: " + timeLeft + "s", ANSI.BRIGHT_WHITE);
        display.drawString(DISPLAY_WIDTH - 24, 1, "Press any key to return", ANSI.WHITE);
        
        display.refreshDisplay();
    }

    public void renderCalibrationScreen(AsciiDisplay display) {
        display.clearBuffer();
        
        // Draw calibration instructions
        display.drawString(2, 1, "Click on the + in the bottom right!", ANSI.BRIGHT_WHITE);
        display.drawString(2, 2, "Make sure the window fits.", ANSI.BRIGHT_WHITE);
        display.setCharacter(DISPLAY_WIDTH-1, DISPLAY_HEIGHT-1, '+', ANSI.BRIGHT_RED);
        
        display.refreshDisplay();
    }
    
    /**
     * Renders the menu title at the top of the screen
     */
    private void renderMenuTitle(AsciiDisplay display) {
        display.drawString(10, 2, "B A T T L E S H I P S", ANSI.BRIGHT_CYAN);
    }
    
    /**
     * Renders the menu options with selected option highlighted
     */
    private void renderMenuOptions(AsciiDisplay display) {
        for (int i = 0; i < MENU_OPTIONS.length; i++) {
            if (i == selectedOption) {
                // Selected option gets bright white on blue background
                display.drawString(10, 5 + i, "> " + MENU_OPTIONS[i], ANSI.BRIGHT_WHITE, ANSI.BLUE);
            } else {
                // Other options get white text
                display.drawString(10, 5 + i, "  " + MENU_OPTIONS[i], ANSI.WHITE);
            }
        }
    }
    
    /**
     * Renders decorative water and ships at the bottom of the main menu
     */
    private void renderWaterDecoration(AsciiDisplay display) {
        // Draw water at the bottom as decoration
        for (int y = 19; y < 24; y++) {
            for (int x = 0; x < 50; x++) {
                // Alternate between regular and bright blue for a wavy effect
                String waveColor = (x + y + Game.getTimeSinceStart().toSeconds()) % 2 == 0 ? 
                    ANSI.BLUE : ANSI.BRIGHT_BLUE;
                display.setCharacter(x, y, '~', waveColor);
            }
        }
        
        // Draw a small ship on the water
        display.drawString(30, 17, ".", ANSI.BRIGHT_WHITE);
        display.drawString(29, 18, "/|\\", ANSI.BRIGHT_WHITE);
        display.drawString(28, 19, "/_|_\\", ANSI.BRIGHT_WHITE);
        display.drawString(33, 19, "_", ANSI.YELLOW);
        display.drawString(29, 20, "\\___/", ANSI.YELLOW);
        
        display.drawString(5, 20, "~~~><>", ANSI.BRIGHT_CYAN);
        display.drawString(40, 22, "><((°>", ANSI.BRIGHT_GREEN);
        display.drawString(15, 23, "><>", ANSI.BRIGHT_MAGENTA);
    }
    
    //----------------------------------------------------------------------------
    // CREDITS SCREEN RENDERING COMPONENTS
    //----------------------------------------------------------------------------
    
    /**
     * Renders a starry background with twinkling stars
     */
    private void renderStarryBackground(AsciiDisplay display) {
        long currentTime = Game.getTimeSinceStart().toMillis();
        for (Star star : stars) {
            // Calculate star visibility based on time (twinkle effect)
            double twinkleValue = Math.sin((currentTime / 300.0) * star.twinkleSpeed);
            if (twinkleValue > 0.2) {
                display.setCharacter(star.x, star.y, star.symbol, star.color);
            } else {
                // When star goes dark, randomize its position at 5% chance
                if (Game.RANDOM.nextDouble() <= 0.05) {
                    star.x = Game.RANDOM.nextInt(DISPLAY_WIDTH);
                    star.y = Game.RANDOM.nextInt(DISPLAY_HEIGHT);
                }
            }
        }
    }
    
    /**
     * Renders animated ships sailing across the screen
     */
    private void renderAnimatedShips(AsciiDisplay display, long currentTime) {
        for (AnimatedShip ship : ships) {
            // Update ship position based on time
            int shipX = (int) ((currentTime / 1000.0 * ship.speed * 12) % (DISPLAY_WIDTH + 20)) - 10;
            
            // Draw ship if it's visible on screen
            if (shipX > -10 && shipX < DISPLAY_WIDTH) {
                for (int i = 0; i < ship.art.length; i++) {
                    display.drawTransparentString(shipX, ship.y + i, ship.art[i], ship.color);
                }
            }
        }
    }
    
    /**
     * Renders animated water waves at the bottom of the screen
     */
    private void renderWaterWaves(AsciiDisplay display, long currentTime) {
        for (int y = DISPLAY_HEIGHT - 4; y < DISPLAY_HEIGHT; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x++) {
                // Create a more complex wave pattern
                double waveOffset = Math.sin(currentTime / 500.0 + x * 0.2) * 0.5;
                char waveChar = (waveOffset > 0) ? '~' : '=';
                
                // Wave color cycling 
                String waveColor = switch ((x + y + (int)(currentTime / 500)) % 3) {
                    case 0 -> ANSI.BRIGHT_BLUE;
                    case 1 -> ANSI.BLUE;
                    default -> ANSI.CYAN;
                };
                
                display.setCharacter(x, y, waveChar, waveColor);
            }
        }
    }
    
    /**
     * Renders scrolling credits text with special effects
     */
    private void renderScrollingCredits(AsciiDisplay display, long currentTime, double progress) {
        // Calculate scrolling position based on progress
        int totalTextHeight = CREDITS_TEXT.length * 2;  // 2 lines per text item
        int startY = DISPLAY_HEIGHT/2 + (int)(totalTextHeight * (1 - progress));
        
        // Draw scrolling credits text - loop in reverse to get proper credits order
        for (int i = CREDITS_TEXT.length - 1; i >= 0; i--) {
            int textY = startY - (CREDITS_TEXT.length - 1 - i) * 2;  // Text position with spacing
            
            // Only draw text if it's within visible area
            if (textY >= 1 && textY < DISPLAY_HEIGHT - 4) {
                String text = CREDITS_TEXT[i];
                int textX = (DISPLAY_WIDTH - text.length()) / 2;
                
                // Special formatting for different text types
                if (text.equals("BATTLESHIPS") || text.equals("THANK YOU FOR PLAYING!")) {
                    renderRainbowText(display, text, textX, textY, currentTime);
                } else if (text.equals("Created by") || text.equals("RedDev")) {
                    renderPulsingText(display, text, textX, textY, currentTime);
                } else if (text.contains("NullPointerException") || text.startsWith("...just kidding")) {
                    display.drawString(textX, textY, text, ANSI.BRIGHT_RED);
                } else {
                    display.drawString(textX, textY, text, ANSI.YELLOW);
                }
            }
        }
    }
    
    //----------------------------------------------------------------------------
    // TEXT RENDERING HELPER METHODS
    //----------------------------------------------------------------------------
    
    /**
     * Renders text with a rainbow color effect
     */
    private void renderRainbowText(AsciiDisplay display, String text, int textX, int textY, long currentTime) {
        for (int j = 0; j < text.length(); j++) {
            double colorPhase = currentTime / 200.0 + j * 0.3;
            String color = switch ((int)colorPhase % 3) {
                case 0 -> ANSI.BRIGHT_CYAN;
                case 1 -> ANSI.BRIGHT_YELLOW;
                default -> ANSI.BRIGHT_WHITE;
            };
            display.setCharacter(textX + j, textY, text.charAt(j), color);
        }
    }
    
    /**
     * Renders text with a pulsing brightness effect
     */
    private void renderPulsingText(AsciiDisplay display, String text, int textX, int textY, long currentTime) {
        double pulse = Math.sin(currentTime / 300.0) * 0.5 + 0.5;
        String color = pulse > 0.5 ? ANSI.BRIGHT_GREEN : ANSI.BRIGHT_YELLOW;
        display.drawString(textX, textY, text, color);
    }
}