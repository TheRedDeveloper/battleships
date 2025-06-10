import java.awt.event.KeyEvent;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

public class BuildMode extends GameMode {
    private static BuildMode instance = null;
    
    private ShipType selectedShipType; // Null if no ship selected
    private Direction selectedDirection = Direction.RIGHT;
    private ShipType[] shipTypeSelectorOnRow = new ShipType[AsciiDisplay.getGridHeight()];
    private Position previewLocation;
    private Boolean isPickUpSelected = false;
    private int pickUpShipRow;
    Position oldPos = new Position(0, 0);
    
    private static final int GRID_START_X = 25;
    private static final int GRID_START_Y = 3;
    private static final int CELL_WIDTH = 2;
    private static final int GRID_SIZE = 10;
    private static final int SHIP_SELECT_START_X = 2;
    private static final int SHIP_SELECT_START_Y = 3;
    
    public static final Map<ShipType, Integer> totalShipTypeCounts = new EnumMap<>(ShipType.class);
    private static final Map<ShipType, Integer> remainingShipTypeCounts = new EnumMap<>(ShipType.class);
    static {
        totalShipTypeCounts.put(ShipType.SUBMARINE1X1, 1);
        totalShipTypeCounts.put(ShipType.DESTROYER2X1, 2);
        totalShipTypeCounts.put(ShipType.CRUISER3X1, 1);
        totalShipTypeCounts.put(ShipType.BATTLESHIP4X1, 1);
        totalShipTypeCounts.put(ShipType.U, 1);

        remainingShipTypeCounts.putAll(totalShipTypeCounts);
    }
    
    private BuildMode() { }

    public static BuildMode getInstance() {
        if (instance == null) {
            instance = new BuildMode();
        }
        return instance;
    }

    @Override
    public GameState enter(GameState gameState) { return gameState; }

    @Override
    public void render(GameState gameState, AsciiDisplay display) {
        display.clearBuffer();


        display.drawString(SHIP_SELECT_START_X, SHIP_SELECT_START_Y-2, "<-/-> to rotate");
        if (remainingShipTypeCounts.values().stream().allMatch(count -> count == 0)) {
            display.drawString(GRID_START_X, SHIP_SELECT_START_Y-2, "Enter to confirm");
        }
        display.drawString(SHIP_SELECT_START_X, SHIP_SELECT_START_Y-1, "Ship Selection:");
        int ry = 1; // "Ship Selection:" is on row 0
        for (ShipType shipType : ShipType.values()) {
            if (remainingShipTypeCounts.get(shipType) > 0) {
                ShipBox box = Ship.boxByType.get(shipType);
                box.displayFromAbsoluteTopLeftOn(SHIP_SELECT_START_X, SHIP_SELECT_START_Y + ry, display, '#', CELL_WIDTH);
                if (remainingShipTypeCounts.get(shipType) > 1) {
                    display.drawString(SHIP_SELECT_START_X + box.getWidth() + 3, SHIP_SELECT_START_Y + ry, "x" + remainingShipTypeCounts.get(shipType).toString());
                }
                for (int i = 0; i < box.getHeight(); i++) {
                    shipTypeSelectorOnRow[ry + i] = shipType;
                }
                ry += box.getHeight() + 1;
                if (shipType == selectedShipType) {
                    display.drawString(SHIP_SELECT_START_X - 2, SHIP_SELECT_START_Y + ry - box.getHeight() - 1, ">");
                }
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
                case KeyEvent.VK_ENTER:
                    if (remainingShipTypeCounts.values().stream().allMatch(count -> count == 0)) {
                        gameState.setMode(MainMode.getInstance());
                    }
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
            double mouseGridXUnrounded = (mousePos.x - GRID_START_X) / (double)CELL_WIDTH;
            double mouseGridYUnrounded = mousePos.y - GRID_START_Y;
            previewLocation = selectedShipType != null ? new Position(
                (int)Math.round(mouseGridXUnrounded - Ship.boxByType.get(selectedShipType).inDirection(selectedDirection).getWidth()/2d),
                (int)Math.round(mouseGridYUnrounded - Ship.boxByType.get(selectedShipType).inDirection(selectedDirection).getHeight()/2d)
            ) : null;
            if (previewLocation != null && (previewLocation.x < 0 || previewLocation.y < 0)) previewLocation = null;

            if (!mousePos.equals(oldPos)) {
                oldPos = mousePos;
                // Game.LOGGER.info(mousePos.toString() + ": " + mouseGridX + ", " + mouseGridY);
            }

            if (isMouseClicked) {
                Grid grid = gameState.grids.get(0);
                if (isPickUpSelected) {
                    Ship ship = grid.getShipAt(mouseGridX, mouseGridY);
                    if (ship != null) {
                        selectedShipType = ship.getType();
                        selectedDirection = ship.getDirection();
                        remainingShipTypeCounts.put(selectedShipType, remainingShipTypeCounts.get(selectedShipType) + 1);
                        grid.removeShip(ship.getId());
                        isPickUpSelected = false;
                    }
                } else if (selectedShipType != null) {
                    int shipX = (int)Math.round(mouseGridXUnrounded - Ship.boxByType.get(selectedShipType).inDirection(selectedDirection).getWidth()/2d);
                    int shipY = (int)Math.round(mouseGridYUnrounded - Ship.boxByType.get(selectedShipType).inDirection(selectedDirection).getHeight()/2d);
                    
                    ShipBox shipBox = Ship.boxByType.get(selectedShipType).inDirection(selectedDirection);
                    boolean isCompletelyInGrid = shipX >= 0 && shipY >= 0 && 
                                                shipBox.getHeight() + shipY <= GRID_SIZE && 
                                                shipBox.getWidth() + shipX <= GRID_SIZE;
                    
                    if (isCompletelyInGrid) {
                        Ship ship = new Ship(selectedShipType, shipX, shipY, selectedDirection);
                        if (!ship.isUsingOccupiedTiles(grid)) {
                            Game.LOGGER.info("Adding ship: " + selectedShipType + " at " + mouseGridX + ", " + mouseGridY);
                            grid.addShip(ship);
                            remainingShipTypeCounts.put(selectedShipType, remainingShipTypeCounts.get(selectedShipType) - 1);
                            selectedShipType = null;
                        }
                    }
                }
            }
        } else {
            previewLocation = null;
            if (!mousePos.equals(oldPos)) {
                oldPos = mousePos;
                // Game.LOGGER.info(mousePos.toString());
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
    }    @Override
    public GameState exit(GameState gameState) {
        gameState.grids.add(gameState.getBotStrategy().generateGrid());
        return gameState;
    }
}