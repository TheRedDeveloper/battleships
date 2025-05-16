import java.time.Duration;

/** Attack mode of the game where the player can attack the opponent's ships.
 *  Handles rendering the attack grid, processing player input for attacks,
 *  and updating the game state based on hit/miss results. */
public class MainMode extends GameMode {
    private static MainMode instance = null;
    public static MainMode getInstance() {
        if (instance == null) {
            instance = new MainMode();
        }
        return instance;
    }

    private static final int MY_GRID_START_X = 25;
    private static final int MY_GRID_START_Y = 3;
    private static final int OPPONENT_GRID_START_X = 2;
    private static final int OPPONENT_GRID_START_Y = 3;
    private static final int CELL_WIDTH = 2;
    private static final int GRID_SIZE = 10;

    private Position previewPosition;

    @Override
    public GameState enter(GameState gameState){ return gameState; }

    @Override
    public void render(GameState gameState, AsciiDisplay display){
        display.clearBuffer();

        Grid myGrid = gameState.grids.get(0);
        Grid opponentGrid = gameState.grids.get(1);

        // My grid
        display.drawString(MY_GRID_START_X, MY_GRID_START_Y - 1, "You", ANSI.YELLOW);
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                String waveColor = (x + y + Game.getTimeSinceStart().toMillis() / 1500) % 2 == 0 ? ANSI.BLUE : ANSI.BRIGHT_BLUE;
                display.setCharacter(MY_GRID_START_X + x * CELL_WIDTH, MY_GRID_START_Y + y, '~', waveColor);
                display.setCharacter(MY_GRID_START_X + x * CELL_WIDTH + 1, MY_GRID_START_Y + y, '~', waveColor);
            }
        }

        for (Ship ship : myGrid.getShips()) {
            ship.displayFromOrigin(MY_GRID_START_X, MY_GRID_START_Y, display, '#', CELL_WIDTH);
        }

        for (Tile tile : myGrid.getHitTiles()) {
            if (tile.isOccupied()) {
                if (!myGrid.getShip(tile.data.containedShip).isSunk()) {
                    display.drawString(MY_GRID_START_X + tile.getX() * CELL_WIDTH, MY_GRID_START_Y + tile.getY(), "XX", ANSI.YELLOW);
                } else {
                    display.drawString(MY_GRID_START_X + tile.getX() * CELL_WIDTH, MY_GRID_START_Y + tile.getY(), "XX", ANSI.BRIGHT_BLACK);
                }
            } else {
                display.drawString(MY_GRID_START_X + tile.getX() * CELL_WIDTH, MY_GRID_START_Y + tile.getY(), "xx", ANSI.GREEN);
            }
        }

        // Opponent grid
        display.drawString(OPPONENT_GRID_START_X, OPPONENT_GRID_START_Y - 1, "Opponent", ANSI.RED);
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                String waveColor = (x + y + Game.getTimeSinceStart().toMillis() / 1500) % 2 == 0 ? ANSI.BLUE : ANSI.BRIGHT_BLUE;
                display.setCharacter(OPPONENT_GRID_START_X + x * CELL_WIDTH, OPPONENT_GRID_START_Y + y, '~', waveColor);
                display.setCharacter(OPPONENT_GRID_START_X + x * CELL_WIDTH + 1, OPPONENT_GRID_START_Y + y, '~', waveColor);
            }
        }

        for (Tile tile : opponentGrid.getHitTiles()) {
            if (tile.isOccupied()) {
            if (!opponentGrid.getShip(tile.data.containedShip).isSunk()) {
                display.drawString(OPPONENT_GRID_START_X + tile.getX() * CELL_WIDTH, OPPONENT_GRID_START_Y + tile.getY(), "XX", ANSI.YELLOW);
            } else {
                display.drawString(OPPONENT_GRID_START_X + tile.getX() * CELL_WIDTH, OPPONENT_GRID_START_Y + tile.getY(), "XX", ANSI.RED);
            }
            } else {
            display.drawString(OPPONENT_GRID_START_X + tile.getX() * CELL_WIDTH, OPPONENT_GRID_START_Y + tile.getY(), "xx", ANSI.GREEN);
            }
        }
        
        if (previewPosition != null) {
            display.drawString(OPPONENT_GRID_START_X + previewPosition.x * CELL_WIDTH, OPPONENT_GRID_START_Y + previewPosition.y, "XX", ANSI.BRIGHT_RED);
        }

        display.refreshDisplay();
    }
    
    @Override
    public GameState update(GameState gameState, InputManager inputManager, Duration deltaTime){
        while (inputManager.hasKeyEvents()) {
            inputManager.pollKeyEvent(); // Just consume events for now
        }

        Position mousePos = inputManager.getMousePosition();
        boolean isMouseClicked = inputManager.checkAndResetMouseClicked();
    
        boolean isMouseInGrid = mousePos.x >= OPPONENT_GRID_START_X &&
                                mousePos.x < GRID_SIZE * CELL_WIDTH + OPPONENT_GRID_START_X && 
                                mousePos.y >= OPPONENT_GRID_START_Y &&
                                mousePos.y < GRID_SIZE + OPPONENT_GRID_START_Y;
        if (isMouseInGrid) {
            int gridMouseX = (mousePos.x - OPPONENT_GRID_START_X) / CELL_WIDTH;
            int gridMouseY = mousePos.y - OPPONENT_GRID_START_Y;
            Grid opponentGrid = gameState.grids.get(1);
            Grid myGrid = gameState.grids.get(0);
            if (!opponentGrid.getTile(gridMouseX, gridMouseY).data.isHit) {
                previewPosition = new Position(gridMouseX, gridMouseY);
                if (isMouseClicked) {
                    opponentGrid.hitTile(gridMouseX, gridMouseY);
                    if (opponentGrid.isLost()) {
                        Game.LOGGER.info("You won!");
                    }
                    Position attackPosition = gameState.getBotStrategy().generateAttackPosition(myGrid);
                    myGrid.hitTile(attackPosition.x, attackPosition.y);
                    if (myGrid.isLost()) {
                        Game.LOGGER.info("You lost!");
                    }
                }
            } else previewPosition = null;
        } else {
            previewPosition = null;
        }
        return gameState;
    }
    
    @Override
    public GameState exit(GameState gameState) { return gameState; }
}
