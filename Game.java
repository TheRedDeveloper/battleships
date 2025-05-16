import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/** The main class */
public class Game {
    private static GameState state = new GameState(
        StartMenuMode.getInstance(),
        new BotStrategy(new RandomGridStrategy(), new CheatAttackStrategy())
    );
    private static boolean isRunning;
    public static final Logger LOGGER = Logger.getLogger(Game.class.getName());
    public static final Random RANDOM = new Random();
    private static Instant startTime;
    private static AsciiDisplay display;
    private static InputManager inputManager;


    public static Duration getTimeSinceStart() {
        return Duration.between(startTime, Instant.now());
    }
    
    static {
        // Configure logger with custom formatter for better readability
        try {
            Logger rootLogger = Logger.getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for(Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            
            ConsoleHandler handler = new ConsoleHandler();
            handler.setFormatter(new GameLogFormatter());
            LOGGER.setUseParentHandlers(false);
            LOGGER.addHandler(handler);
        } catch (SecurityException se) {
            LOGGER.severe("Security exception during logger setup: " + se.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Failed to configure custom logging: " + e.getMessage());
        }
    }

    public static void play() {
        try {
            LOGGER.info("Starting game...");
            isRunning = true;
            startTime = Instant.now();
            
            LOGGER.info("Initializing display...");
            inputManager = new InputManager();
            display = new AsciiDisplay(inputManager);
            
            LOGGER.info("Showing display window...");
            display.show();
            
            LOGGER.info("Waiting for UI initialization...");
            Thread.sleep(1000);
            
            StartMenuMode.getInstance().enter(state);
            
            LOGGER.info("Entering game loop...");
            Instant lastTime = Instant.now();
            while (isRunning) {
                state.getMode().render(state, display);
                state = state.getMode().update(state, inputManager, Duration.between(lastTime, Instant.now()));
                lastTime = Instant.now();
                
                Thread.sleep(50);
            }
        } catch (Exception e) {
            LOGGER.severe("Error in game loop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stop() {
        Game.LOGGER.info("Stopping game...");
        isRunning = false;
    }
    
    public static void main(String[] args) {
        Game.LOGGER.info("Game main method called");
        
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.severe("Failed to set look and feel: " + e.getMessage());
        }
        
        Game.play();
    }

    public static void resizeDisplay(int width, int height) {
        display.resize(width, height);
    }

    public static void calibrateDisplay(int calibrationX, int calibrationY) {
        display.calibrate(calibrationX, calibrationY);
    }
}
