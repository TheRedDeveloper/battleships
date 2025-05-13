import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.logging.Level;

public class BuildMode extends GameMode {
    private static BuildMode instance = null;
    
    private ShipType selectedShipType; // Null if no ship selected
    private Direction selectedDirection = Direction.RIGHT;
    private ShipType[] shipTypeSelectorOnRow = new ShipType[AsciiDisplay.getGridHeight()];
    private Position previewLocation = new Position(0, 0);
    private Boolean isPickUpSelected = false;
    private int pickUpShipRow;

    Position oldPos = new Position(0, 0);
    
    private static final int GRID_START_X = 25;
    private static final int GRID_START_Y = 3;
    private static final int CELL_WIDTH = 2;
    private static final int GRID_SIZE = 10;
    private static final int SHIP_SELECT_START_X = 2;
    private static final int SHIP_SELECT_START_Y = 3;
    
    private BuildMode() {
    }

    public static BuildMode getInstance() {
        if (instance == null) {
            instance = new BuildMode();
        }
        return instance;
    }

    @Override
    public void enter() {
    }

    @Override
    public void render(GameState gameState, AsciiDisplay display) {
        display.clearBuffer();

        display.drawString(SHIP_SELECT_START_X, SHIP_SELECT_START_Y, "Ship Selection:");
        int ry = 1; // "Ship Selection:" is on row 0
        for (ShipType shipType : ShipType.values()) {
            ShipBox box = Ship.boxByType.get(shipType);
            box.displayFromAbsoluteTopLeftOn(SHIP_SELECT_START_X, SHIP_SELECT_START_Y + ry, display, '#', CELL_WIDTH);
            for (int i = 0; i < box.getHeight(); i++) {
                shipTypeSelectorOnRow[ry + i] = shipType;
            }
            ry += box.getHeight() + 1;
            if (shipType == selectedShipType) {
                display.drawString(SHIP_SELECT_START_X - 2, SHIP_SELECT_START_Y + ry - box.getHeight() - 1, ">");
            }
        }
        display.drawString(SHIP_SELECT_START_X, SHIP_SELECT_START_Y + ry, "Pick up");
        if (isPickUpSelected) {
            display.drawString(SHIP_SELECT_START_X - 2, SHIP_SELECT_START_Y + ry, ">");
        }
        pickUpShipRow = SHIP_SELECT_START_Y + ry;

        // Fill grid with blue water
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                String waveColor = (x + y + Game.getTimeSinceStart().toMillis() / 1500) % 2 == 0 ? ANSI.BLUE : ANSI.BRIGHT_BLUE;
                display.setCharacter(GRID_START_X + x * CELL_WIDTH, GRID_START_Y + y, '~', waveColor);
                display.setCharacter(GRID_START_X + x * CELL_WIDTH + 1, GRID_START_Y + y, '~', waveColor);
            }
        }

        // Draw preview ship
        if (selectedShipType != null && previewLocation != null) {
            Ship ship = new Ship(selectedShipType, previewLocation.x, previewLocation.y, selectedDirection);
            if(ship.getShipBox().getHeight() + previewLocation.y <= GRID_SIZE && ship.getShipBox().getWidth() + previewLocation.x <= GRID_SIZE) {
                char displayChar = ship.isUsingOccupiedTiles(gameState.grids.get(0)) ? 'X' : '0';
                ship.displayFromOrigin(GRID_START_X, GRID_START_Y, display, displayChar, CELL_WIDTH);
            }
        }

        for (Ship ship : gameState.grids.get(0).getShips()) {
            ship.displayFromOrigin(GRID_START_X, GRID_START_Y, display, '#', CELL_WIDTH);
        }

        display.refreshDisplay();
    }
    
    @Override
    public GameState update(GameState gameState, InputManager inputManager, Duration deltaTime) {
        while (inputManager.hasKeyEvents()) {
            KeyEvent event = inputManager.pollKeyEvent();
            switch (event.getKeyCode()) {
                case KeyEvent.VK_L, KeyEvent.VK_RIGHT:
                    selectedDirection = selectedDirection.rotated(RotationDirection.CLOCKWISE);
                    break;
                case KeyEvent.VK_H, KeyEvent.VK_LEFT:
                    selectedDirection = selectedDirection.rotated(RotationDirection.COUNTER_CLOCKWISE);
                    break;
                default:
                    break;
            }

        }
        
        Position mousePos = inputManager.getMousePosition();
        boolean isMouseClicked = inputManager.checkAndResetMouseClicked();
        
        boolean isMouseInGrid = mousePos.x >= GRID_START_X &&
                                mousePos.x < GRID_SIZE * CELL_WIDTH + GRID_START_X && 
                                mousePos.y >= GRID_START_Y &&
                                mousePos.y < GRID_SIZE + GRID_START_Y;
        
        if (isMouseInGrid) {
            int mouseGridX = (mousePos.x - GRID_START_X) / CELL_WIDTH;
            int mouseGridY = mousePos.y - GRID_START_Y;
            previewLocation = selectedShipType != null ? new Position(mouseGridX, mouseGridY) : null;

            if (!mousePos.equals(oldPos)) {
                oldPos = mousePos;
                Game.LOGGER.log(Level.INFO, mousePos.toString() + ": " + mouseGridX + ", " + mouseGridY);
            }

            if (isMouseClicked) {
                Grid grid = gameState.grids.get(0);
                if (isPickUpSelected) {
                    Ship ship = grid.getShipAt(mouseGridX, mouseGridY);
                    if (ship != null) {
                        selectedShipType = ship.getType();
                        selectedDirection = ship.getDirection();
                        grid.removeShip(ship.getId());
                        isPickUpSelected = false;
                    }
                } else if (selectedShipType != null) {
                    Ship ship = new Ship(selectedShipType, mouseGridX, mouseGridY, selectedDirection);
                    if (ship.getShipBox().getHeight() + mouseGridY <= GRID_SIZE &&
                        ship.getShipBox().getWidth() + mouseGridX <= GRID_SIZE &&
                        !ship.isUsingOccupiedTiles(grid)) {
                        
                        Game.LOGGER.log(Level.INFO, "Adding ship: " + selectedShipType + " at " + mouseGridX + ", " + mouseGridY);
                        grid.addShip(ship);
                        selectedShipType = null;
                    }
                }
            }
        } else {
            previewLocation = null;
            if (!mousePos.equals(oldPos)) {
                oldPos = mousePos;
                Game.LOGGER.log(Level.INFO, mousePos.toString());
            }

            if (mousePos.y == pickUpShipRow && isMouseClicked) {
                isPickUpSelected = true;
                selectedShipType = null;
            } else if (mousePos.y >= SHIP_SELECT_START_Y && 
                mousePos.y < SHIP_SELECT_START_Y + shipTypeSelectorOnRow.length) {
                
                ShipType hoveredType = shipTypeSelectorOnRow[mousePos.y - SHIP_SELECT_START_Y];
                if (hoveredType != null && isMouseClicked) {
                    selectedShipType = hoveredType;
                    selectedDirection = Direction.RIGHT;
                    isPickUpSelected = false;
                }
            }
        }
    
        return gameState;
    }
    
    @Override
    public void exit() {
    }
}