import java.util.List;
import java.util.logging.Level;

/** The global state of the game, persistent between modes. */
public class GameState {
    private GameMode mode;
    public List<Grid> grids;

    public GameState(GameMode mode) {
        this.mode = mode;
        this.grids = List.of(new Grid("Player"));
    }

    public void setMode(GameMode mode) {
        Game.LOGGER.log(Level.INFO, "Switching game mode from " + this.mode.getClass().getSimpleName() + " to " + mode.getClass().getSimpleName());
        this.mode.exit();
        this.mode = mode;
        mode.enter();
    }

    public GameMode getMode() {
        if (mode == null) {
            throw new IllegalStateException("Game mode is not set. Call setMode() first.");
        }
        return mode;
    }
}
