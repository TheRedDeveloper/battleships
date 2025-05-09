/** SINGLETON INTERFACE */
public abstract class GameMode {
    protected GameMode() {
        // Protected constructor for singleton pattern
    }

    public abstract void enter();
    
    public abstract void render(GameState gameState, AsciiDisplay display);
    
    public abstract GameState update(GameState gameState, InputManager inputManager);
    
    public abstract void exit();
}
