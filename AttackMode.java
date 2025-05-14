import java.time.Duration;

/**
 * Attack mode of the game where the player can attack the opponent's ships.
 * Handles rendering the attack grid, processing player input for attacks,
 * and updating the game state based on hit/miss results.
 */
public class AttackMode extends GameMode {
    private static AttackMode instance = null;
    public static AttackMode getInstance() {
        if (instance == null) {
            instance = new AttackMode();
        }
        return instance;
    }

    private static final int MY_GRID_START_X = 25;
    private static final int MY_GRID_START_Y = 3;
    private static final int CELL_WIDTH = 2;
    private static final int GRID_SIZE = 10;

    private Position previewPosition;

    public void enter(){}
    
    public void render(GameState gameState, AsciiDisplay display){
        display.clearBuffer();

        // Fill grid with blue water
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                String waveColor = (x + y + Game.getTimeSinceStart().toMillis() / 1500) % 2 == 0 ? ANSI.BLUE : ANSI.BRIGHT_BLUE;
                display.setCharacter(MY_GRID_START_X + x * CELL_WIDTH, MY_GRID_START_Y + y, '~', waveColor);
                display.setCharacter(MY_GRID_START_X + x * CELL_WIDTH + 1, MY_GRID_START_Y + y, '~', waveColor);
            }
        }

        for (Ship ship : gameState.grids.get(0).getShips()) {
            if (ship.isSunk()) {
                ship.displayFromOrigin(MY_GRID_START_X, MY_GRID_START_Y, display, 'X', CELL_WIDTH);
            } else {
                ship.displayFromOrigin(MY_GRID_START_X, MY_GRID_START_Y, display, '#', CELL_WIDTH);
            }
        }

        for (Tile tile : gameState.grids.get(0).getHitTiles()) {
            display.setCharacter(MY_GRID_START_X + tile.getX(), MY_GRID_START_Y + tile.getY(), 'X', ANSI.RED);
        }

        display.refreshDisplay();
    }
    
    public GameState update(GameState gameState, InputManager inputManager, Duration deltaTime){
        
        return gameState;
    }
    
    public void exit(){}
}
