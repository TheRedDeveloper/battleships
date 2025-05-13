import java.time.Duration;

/** A singleton interface for game modes. */
public abstract class GameMode {
    protected GameMode() {
        // Protected constructor for singleton pattern
    }

    public abstract void enter();
    
    public abstract void render(GameState gameState, AsciiDisplay display);
    
    public abstract GameState update(GameState gameState, InputManager inputManager, Duration deltaTime);
    
    public abstract void exit();
}
