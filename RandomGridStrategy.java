import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/** Responsible for randomly placing ships on the bot's grid.
 * 
 * Uses the same ship counts and validation rules as {@link BuildMode} to ensure
 * fairness while adding randomization for replayability.
 *
 * @author Claude */
public class RandomGridStrategy implements GridStrategy {
    private static final int GRID_SIZE = 10;
    private static final Random random = new Random();
    
    // Ship type counts matching those in BuildMode
    private static final Map<ShipType, Integer> shipTypeCounts = new EnumMap<>(ShipType.class);
    static {
        shipTypeCounts.put(ShipType.SUBMARINE1X1, 1);
        shipTypeCounts.put(ShipType.DESTROYER2X1, 2);
        shipTypeCounts.put(ShipType.CRUISER3X1, 1);
        shipTypeCounts.put(ShipType.BATTLESHIP4X1, 1);
        shipTypeCounts.put(ShipType.U, 1);
    }
    
    @Override
    public Grid generateGrid() {
        Grid grid = new Grid("Bot");
        
        // Generate ships for each type according to their counts
        for (ShipType shipType : ShipType.values()) {
            int count = shipTypeCounts.get(shipType);
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
            
            int x = random.nextInt(GRID_SIZE);
            int y = random.nextInt(GRID_SIZE);
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
        return directions[random.nextInt(directions.length)];
    }
}
