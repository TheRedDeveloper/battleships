
/** Responsible for randomly placing ships on the bot's grid.
 * 
 * Uses the same ship counts and validation rules as {@link BuildMode} to ensure
 * fairness while adding randomization for replayability.
 *
 * @author Claude */
public class RandomGridStrategy implements GridStrategy {
    private static final int GRID_SIZE = 10;
    
    @Override
    public Grid generateGrid() {
        Grid grid = new Grid("Bot");
        
        // Generate ships for each type according to their counts
        for (ShipType shipType : ShipType.values()) {
            int count = BuildMode.totalShipTypeCounts.get(shipType);
            for (int i = 0; i < count; i++) {
                placeShipRandomly(grid, shipType);
            }
        }
        
        Game.LOGGER.info("Generated random bot grid with " + grid.getShips().size() + " ships");
        return grid;
    }
    
    /** Attempts to find a valid random position for a ship.
     *  
     * Uses retry mechanism because the random placement might result in invalid positions 
     * due to grid boundaries or ship overlaps. */
    private void placeShipRandomly(Grid grid, ShipType shipType) {
        boolean placed = false;
        int attempts = 0;
        final int MAX_ATTEMPTS = 1000; // Prevent infinite loops
        
        while (!placed && attempts < MAX_ATTEMPTS) {
            attempts++;
            
            int x = Game.RANDOM.nextInt(GRID_SIZE);
            int y = Game.RANDOM.nextInt(GRID_SIZE);
            Direction direction = getRandomDirection();
            
            Ship ship = new Ship(shipType, x, y, direction);
            ShipBox shipBox = ship.getShipBox();
            
            // Check if ship is within grid bounds
            boolean withinBounds = x >= 0 && y >= 0 && 
                                    x + shipBox.getWidth() <= GRID_SIZE && 
                                    y + shipBox.getHeight() <= GRID_SIZE;
            
            if (withinBounds && !ship.isUsingOccupiedTiles(grid)) {
                grid.addShip(ship);
                placed = true;
                Game.LOGGER.info("Placed " + shipType + " at " + x + "," + y + " with direction " + direction);
            }
        }
        
        if (!placed) {
            throw new IllegalStateException("Failed to place " + shipType + " after " + MAX_ATTEMPTS + " attempts");
        }
    }
    
    /** Picks a random direction for ship placement.
     * 
     * Random orientation makes the grid less predictable. */
    private Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[Game.RANDOM.nextInt(directions.length)];
    }
}
