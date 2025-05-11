import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** The main class */
public class Game {
    private static GameState state = new GameState(StartMenuMode.getInstance());
    private static boolean isRunning;
    public static final Logger LOGGER = Logger.getLogger(Game.class.getName());
    
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
            LOGGER.log(Level.SEVERE, "Security exception during logger setup: {0}", se.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to configure custom logging: {0}", e.getMessage());
        }
    }

    public static void play() {
        try {
            LOGGER.log(Level.INFO, "Starting game...");
            isRunning = true;
            
            LOGGER.log(Level.INFO, "Initializing display...");
            InputManager inputManager = new InputManager();
            AsciiDisplay display = new AsciiDisplay(inputManager);
            
            LOGGER.log(Level.INFO, "Showing display window...");
            display.show();
            
            LOGGER.log(Level.INFO, "Waiting for UI initialization...");
            Thread.sleep(1000);
            
            StartMenuMode.getInstance().enter();
            
            LOGGER.log(Level.INFO, "Entering game loop...");
            while (isRunning) {
                state.getMode().render(state, display);
                state = state.getMode().update(state, inputManager);
                
                Thread.sleep(50);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in game loop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stop() {
        Game.LOGGER.log(Level.INFO, "Stopping game...");
        isRunning = false;
    }
    
    public static void main(String[] args) {
        Game.LOGGER.log(Level.INFO, "Game main method called");
        
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to set look and feel: " + e.getMessage());
        }
        
        Game.play();
    }
}
