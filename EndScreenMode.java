import java.awt.event.KeyEvent;
import java.time.Duration;

/** End screen showing game results (victory or defeat).
 *
 * This screen displays when a game concludes, showing victory or defeat
 * animations and allowing the player to return to the start menu.
 *
 * @author Copilot */
public class EndScreenMode extends GameMode {
    // Constants and member variables
    private static final int DISPLAY_HEIGHT = AsciiDisplay.getGridHeight();
    private static final int DISPLAY_WIDTH = AsciiDisplay.getGridWidth();
    private static final long ANIMATION_DURATION_MS = 10000; // 10 second animation
    private long startTime = 0;
    private boolean playerWon = false;
    
    // Victory/defeat messages
    private static final String[] VICTORY_TEXT = {
        "VICTORY!",
        "",
        "You sank all enemy ships!",
        "",
        "You are the admiral of the seas!",
        "",
        "Press any key to return to main menu"
    };
    
    private static final String[] DEFEAT_TEXT = {
        "DEFEAT",
        "",
        "Your fleet has been destroyed",
        "",
        "Better luck next time, captain",
        "",
        "Press any key to return to main menu"
    };
    
    // Animation elements
    private static class Particle {
        int x;
        int y;
        double velocityX;
        double velocityY;
        char symbol;
        String color;
        double lifeTime;
        double maxLifeTime;
        
        Particle(int x, int y, double velocityX, double velocityY, 
                char symbol, String color, double maxLifeTime) {
            this.x = x;
            this.y = y;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.symbol = symbol;
            this.color = color;
            this.lifeTime = 0;
            this.maxLifeTime = maxLifeTime;
        }
        
        boolean update(double deltaTime) {
            x += velocityX * deltaTime;
            y += velocityY * deltaTime;
            lifeTime += deltaTime;
            return lifeTime < maxLifeTime && x >= 0 && x < DISPLAY_WIDTH && y >= 0 && y < DISPLAY_HEIGHT;
        }
    }
    
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
    
    // Particles for victory/defeat animations
    private static final int MAX_PARTICLES = 150;
    private Particle[] particles = new Particle[MAX_PARTICLES];
    private int activeParticles = 0;
    
    private static EndScreenMode instance = null;
    
    // Singleton pattern implementation
    public static EndScreenMode getInstance() {
        if (instance == null) {
            instance = new EndScreenMode();
        }
        return instance;
    }
    
    @Override
    public GameState enter(GameState gameState) {
        // Check if player won or lost - player is grid 0, opponent is grid 1
        playerWon = gameState.grids.get(1).isLost();
        startTime = Game.getTimeSinceStart().toMillis();
        
        // Initialize stars for background
        initializeStars();
        
        // Initialize particles for animations
        initializeParticles();
        
        return gameState;
    }
    
    @Override
    public void render(GameState gameState, AsciiDisplay display) {
        display.clearBuffer();
        
        // Calculate animation progress
        long currentTime = Game.getTimeSinceStart().toMillis();
        long elapsedTime = currentTime - startTime;
        double progress = Math.min(1.0, (double) elapsedTime / ANIMATION_DURATION_MS);
        
        // Render background and animations
        renderStarryBackground(display, currentTime);
        renderParticles(display, progress, currentTime);
        renderMessageText(display, currentTime);
        
        display.refreshDisplay();
    }
    
    @Override
    public GameState update(GameState gameState, InputManager inputManager, Duration deltaTime) {
        // Update particles
        updateParticles(deltaTime.toMillis() / 100.0);
        
        // Check for key press to return to main menu
        while (inputManager.hasKeyEvents()) {
            KeyEvent keyEvent = inputManager.pollKeyEvent();
            if (keyEvent != null) {
                // Any key returns to main menu
                gameState.setMode(StartMenuMode.getInstance());
                break;
            }
        }
        
        // Check for mouse click to return to main menu
        if (inputManager.checkAndResetMouseClicked()) {
            gameState.setMode(StartMenuMode.getInstance());
        }
        
        return gameState;
    }
    
    @Override
    public GameState exit(GameState gameState) {
        return gameState;
    }
    
    /**
     * Initialize stars with random positions for background effects
     */
    private void initializeStars() {
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
     * Initialize particles for animations
     */
    private void initializeParticles() {
        activeParticles = 0;
        
        // Different particle configurations based on victory or defeat
        if (playerWon) {
            initializeVictoryParticles();
        } else {
            initializeDefeatParticles();
        }
    }
    
    /**
     * Initialize colorful celebration particles for victory
     */
    private void initializeVictoryParticles() {
        String[] celebrationColors = {
            ANSI.BRIGHT_YELLOW, ANSI.BRIGHT_GREEN, ANSI.BRIGHT_BLUE, 
            ANSI.BRIGHT_MAGENTA, ANSI.BRIGHT_CYAN
        };
        
        // Create random firework particles across the top of the screen
        for (int i = 0; i < MAX_PARTICLES; i++) {
            int x = Game.RANDOM.nextInt(DISPLAY_WIDTH);
            int y = Game.RANDOM.nextInt(DISPLAY_HEIGHT / 2);
            
            double vx = (Game.RANDOM.nextDouble() - 0.5) * 3.0;
            double vy = (Game.RANDOM.nextDouble() - 0.5) * 3.0;
            
            char symbol = "*+•·".charAt(Game.RANDOM.nextInt(4));
            String color = celebrationColors[Game.RANDOM.nextInt(celebrationColors.length)];
            double life = 10.0 + Game.RANDOM.nextDouble() * 40.0;
            
            particles[activeParticles++] = new Particle(x, y, vx, vy, symbol, color, life);
            if (activeParticles >= MAX_PARTICLES) break;
        }
    }
    
    /**
     * Initialize dark smoke-like particles for defeat
     */
    private void initializeDefeatParticles() {
        String[] defeatColors = {
            ANSI.BLACK, ANSI.BRIGHT_BLACK, ANSI.RED, ANSI.BRIGHT_RED
        };
        
        // Create smoke particles rising from the bottom of the screen
        for (int i = 0; i < MAX_PARTICLES; i++) {
            int x = Game.RANDOM.nextInt(DISPLAY_WIDTH);
            int y = DISPLAY_HEIGHT - Game.RANDOM.nextInt(10);
            
            double vx = (Game.RANDOM.nextDouble() - 0.5) * 0.5;
            double vy = -Game.RANDOM.nextDouble() * 2.0;
            
            char symbol = "°*•~".charAt(Game.RANDOM.nextInt(4));
            String color = defeatColors[Game.RANDOM.nextInt(defeatColors.length)];
            double life = 10.0 + Game.RANDOM.nextDouble() * 30.0;
            
            particles[activeParticles++] = new Particle(x, y, vx, vy, symbol, color, life);
            if (activeParticles >= MAX_PARTICLES) break;
        }
    }
    
    /**
     * Render a starry background with twinkling effect
     */
    private void renderStarryBackground(AsciiDisplay display, long currentTime) {
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
     * Update all particle positions and lifetimes
     */
    private void updateParticles(double deltaTime) {
        int aliveCount = 0;
        
        // Update existing particles
        for (int i = 0; i < activeParticles; i++) {
            if (particles[i] != null && particles[i].update(deltaTime)) {
                // Particle still alive, keep it
                particles[aliveCount++] = particles[i];
            }
        }
        
        // Update count of active particles
        activeParticles = aliveCount;
        
        // Add new particles if needed
        if (activeParticles < MAX_PARTICLES / 3) {
            if (playerWon) {
                addVictoryParticles(5);
            } else {
                addDefeatParticles(5);
            }
        }
    }
    
    /**
     * Add additional victory particles
     */
    private void addVictoryParticles(int count) {
        String[] celebrationColors = {
            ANSI.BRIGHT_YELLOW, ANSI.BRIGHT_GREEN, ANSI.BRIGHT_CYAN
        };
        
        for (int i = 0; i < count; i++) {
            if (activeParticles >= MAX_PARTICLES) break;
            
            int x = Game.RANDOM.nextInt(DISPLAY_WIDTH);
            int y = Game.RANDOM.nextInt(5);
            
            double vx = (Game.RANDOM.nextDouble() - 0.5) * 2.0;
            double vy = (Game.RANDOM.nextDouble()) * 2.0;
            
            char symbol = "*+•·".charAt(Game.RANDOM.nextInt(4));
            String color = celebrationColors[Game.RANDOM.nextInt(celebrationColors.length)];
            double life = 10.0 + Game.RANDOM.nextDouble() * 20.0;
            
            particles[activeParticles++] = new Particle(x, y, vx, vy, symbol, color, life);
        }
    }
    
    /**
     * Add additional defeat particles
     */
    private void addDefeatParticles(int count) {
        String[] defeatColors = {
            ANSI.BLACK, ANSI.BRIGHT_BLACK, ANSI.RED
        };
        
        for (int i = 0; i < count; i++) {
            if (activeParticles >= MAX_PARTICLES) break;
            
            int x = Game.RANDOM.nextInt(DISPLAY_WIDTH);
            int y = DISPLAY_HEIGHT - 1;
            
            double vx = (Game.RANDOM.nextDouble() - 0.5) * 0.5;
            double vy = -Game.RANDOM.nextDouble() * 1.5;
            
            char symbol = "°*•~".charAt(Game.RANDOM.nextInt(4));
            String color = defeatColors[Game.RANDOM.nextInt(defeatColors.length)];
            double life = 5.0 + Game.RANDOM.nextDouble() * 30.0;
            
            particles[activeParticles++] = new Particle(x, y, vx, vy, symbol, color, life);
        }
    }
    
    /**
     * Render all active particles
     */
    private void renderParticles(AsciiDisplay display, double progress, long currentTime) {
        for (int i = 0; i < activeParticles; i++) {
            if (particles[i] != null) {
                display.setCharacter(particles[i].x, particles[i].y, particles[i].symbol, particles[i].color);
            }
        }
    }
    
    /**
     * Render victory or defeat message
     */
    private void renderMessageText(AsciiDisplay display, long currentTime) {
        String[] messageText = playerWon ? VICTORY_TEXT : DEFEAT_TEXT;
        
        int startY = (DISPLAY_HEIGHT - messageText.length) / 2 - 2;
        
        for (int i = 0; i < messageText.length; i++) {
            String text = messageText[i];
            int textX = (DISPLAY_WIDTH - text.length()) / 2;
            
            if (i == 0) {
                // Title text with special effect
                renderRainbowOrFlickerText(display, text, textX, startY + i, currentTime);
            } else {
                // Regular text
                String color = playerWon ? ANSI.BRIGHT_WHITE : ANSI.WHITE;
                display.drawString(textX, startY + i, text, color);
            }
        }
    }
    
    /**
     * Render text with rainbow effect for victory or flicker effect for defeat
     */
    private void renderRainbowOrFlickerText(AsciiDisplay display, String text, int textX, int textY, long currentTime) {
        if (playerWon) {
            // Rainbow effect for victory
            for (int j = 0; j < text.length(); j++) {
                double colorPhase = currentTime / 200.0 + j * 0.3;
                String color = switch ((int)colorPhase % 6) {
                    case 0 -> ANSI.BRIGHT_RED;
                    case 1 -> ANSI.BRIGHT_YELLOW;
                    case 2 -> ANSI.BRIGHT_GREEN;
                    case 3 -> ANSI.BRIGHT_CYAN;
                    case 4 -> ANSI.BRIGHT_BLUE;
                    default -> ANSI.BRIGHT_MAGENTA;
                };
                display.setCharacter(textX + j, textY, text.charAt(j), color);
            }
        } else {
            // Flicker effect for defeat
            double flickerIntensity = Math.sin(currentTime / 200.0) * 0.5 + 0.5;
            String color = flickerIntensity > 0.5 ? ANSI.BRIGHT_RED : ANSI.RED;
            display.drawString(textX, textY, text, color);
        }
    }
}
