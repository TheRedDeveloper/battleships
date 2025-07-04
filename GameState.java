import java.util.ArrayList;
import java.util.List;

/** The global state of the game, persistent between modes. */
public class GameState {
    private GameMode mode;
    private BotStrategy botStrategy;
    public List<Grid> grids;

    public GameState(GameMode mode, BotStrategy botStrategy) {
        this.mode = mode;
        this.grids = new ArrayList<>();
        this.grids.add(new Grid("Player"));
        this.botStrategy = botStrategy;
    }

    public void setMode(GameMode mode) {
        Game.LOGGER.info("Switching game mode from " + this.mode.getClass().getSimpleName() + " to " + mode.getClass().getSimpleName());
        set(this.mode.exit(this));
        this.mode = mode;
        set(mode.enter(this));
    }

    public void set(GameState gameState) {
        this.mode = gameState.mode;
        this.grids = gameState.grids;
        this.botStrategy = gameState.botStrategy;
    }

    public GameMode getMode() {
        if (mode == null) {
            throw new IllegalStateException("Game mode is not set. Call setMode() first.");
        }
        return mode;
    }

    public void setBotStrategy(BotStrategy botStrategy) {
        Game.LOGGER.info("Bot strategy set to " + botStrategy.getClass().getSimpleName());
        this.botStrategy = botStrategy;
    }

    public BotStrategy getBotStrategy() {
        if (botStrategy == null) {
            throw new IllegalStateException("Bot strategy is not set. Call setBotStrategy() first.");
        }
        return botStrategy;
    }
}
