import java.time.Duration;

/** A singleton interface for game modes. */
public abstract class GameMode {
    protected GameMode() {
        // Protected constructor for singleton pattern
    }

    public abstract GameState enter(GameState gameState);
    
    public abstract void render(GameState gameState, AsciiDisplay display);
    
    public abstract GameState update(GameState gameState, InputManager inputManager, Duration deltaTime);
    
    public abstract GameState exit(GameState gameState);
}
