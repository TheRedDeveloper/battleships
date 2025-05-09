public class BuildMode extends GameMode {
    private static BuildMode instance = null;
    
    private ShipType selectedShipType; // Null if no ship selected
    private ShipType[] shipTypeSelectorOnRow = new ShipType[10];
    
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

        // Draw the ship selection on the left
        display.drawString(SHIP_SELECT_START_X, SHIP_SELECT_START_Y, "Ship Selection:");
        int ry = 0; // Relative Y where to draw the ship type selection area
        for (ShipType shipType : ShipType.values()) {
            ShipBox box = Ship.boxByType.get(shipType);
            for (int i = 0; i < box.getHeight(); i++) {
                shipTypeSelectorOnRow[ry + i] = shipType;
            }
            ry += box.getHeight() + 1;
        }
    }
    
    @Override
    public GameState update(GameState gameState, InputManager inputManager) {
        // "Process" key events (get them out of the way)
        while (inputManager.hasKeyEvents()) inputManager.pollKeyEvent();
        
        int[] mousePos = inputManager.getMousePosition();
        boolean isMouseClicked = inputManager.checkAndResetMouseClicked();
        
        boolean isMouseInGrid = (mousePos[0] >= GRID_START_X && mousePos[0] < GRID_SIZE * CELL_WIDTH + GRID_START_X) 
                && (mousePos[1] >= GRID_START_Y && mousePos[1] < GRID_SIZE + GRID_START_Y);
        if (isMouseInGrid) {
            int mouseGridX = (mousePos[0] - GRID_START_X) / CELL_WIDTH;
            int mouseGridY = mousePos[1] - GRID_START_Y;
            
            if (isMouseClicked) {
                // Handle ship placement logic
            }
        } else {
            // Check if mouse is over ship selection area
            for (int i = 0; i < shipTypeSelectorOnRow.length; i++) {
                ShipType shipType = shipTypeSelectorOnRow[i];
                ShipBox box = Ship.boxByType.get(shipType);
                if (mousePos[0] >= SHIP_SELECT_START_X && mousePos[0] < SHIP_SELECT_START_X + box.getWidth() 
                        && mousePos[1] >= SHIP_SELECT_START_Y + i && mousePos[1] < SHIP_SELECT_START_Y + i + box.getHeight()) {
                    if (isMouseClicked) {
                        selectedShipType = shipType;
                    }
                }
            }
        }

        // Mouse not over grid  
        // Check if mouse clicked on ship selection areas

    
        return gameState;
    }
    
    @Override
    public void exit() {
    }
}