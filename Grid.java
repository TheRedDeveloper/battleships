import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Grid {
    public TileData[][] tiles;
    private Map<UUID, Ship> ships;
    private String name;

    public Grid(String name) {
        this.name = name;
        tiles = new TileData[10][10];
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                tiles[x][y] = new TileData(false, null);
            }
        }
        ships = new HashMap<>(); // I hate Java, I DON'T NEED HASHING BUT I HAVE TO USE IT
    }

    public Tile getTile(int x, int y) {
        return new Tile(tiles[x][y], new Position(x, y));
    }

    public void setTile(int x, int y, TileData tileData) {
        tiles[x][y] = tileData;
    }

    public String getName() { return name; }
    public Collection<Ship> getShips() { return ships.values(); }

    public void updateShip(UUID id, Ship ship) { 
        if (!ships.containsKey(id)) { throw new IllegalArgumentException("Can't update Ship: Ship with ID " + id + " does not exist."); }
        if (id != ship.getId()) { throw new IllegalArgumentException("Can't update Ship: ID " + id + " does not match ship ID to update " + ship.getId()); }
        if (ship.getType() != ships.get(id).getType()) { // Yes, this is a valid operation, I decided to allow
            removeOccupation(ships.get(id));
            addOccupation(ship);
        }
        if (ships.get(id) == ship) { return; }
        ships.put(id, ship);
    }

    public void addShip(Ship ship) {
        if (ships.containsKey(ship.getId())) { throw new IllegalArgumentException("Can't add Ship: Ship with ID " + ship.getId() + " already exists."); }
        addOccupation(ship);
        ships.put(ship.getId(), ship);
    }

    public void removeShip(UUID id) {
        if (!ships.containsKey(id)) { throw new IllegalArgumentException("Can't remove Ship: Ship with ID " + id + " does not exist."); }
        removeOccupation(ships.get(id));
        ships.remove(id);
    }

    public Ship getShip(UUID id) {
        if (!ships.containsKey(id)) { throw new IllegalArgumentException("Can't get Ship: Ship with ID " + id + " does not exist."); }
        return ships.get(id);
    }

    public Ship getShipAt(int x, int y) {
        UUID id = tiles[x][y].containedShip;
        if (id == null) { return null; }
        return getShip(id);
    }

    private void removeOccupation(Ship ship) {
        for (Position pos : ship.getOccupiedPositions()) {
            if (tiles[pos.x][pos.y].containedShip == null) {
                // We don't reverse any of the damage already done. This is an illegal state.
                // The tiles mark an invalid ship if the error is suppressed.
                throw new IllegalStateException("Tile at " + pos + " does not contain a ship, can't remove here. INVALID STATE CAUSED. DO NOT SUPPRESS THIS ERROR.");
            }
            tiles[pos.x][pos.y].containedShip = null;
        }
    }

    private void addOccupation(Ship ship) {
        for (Position pos : ship.getOccupiedPositions()) {
            if (tiles[pos.x][pos.y].containedShip != null) {
                // We don't reverse any of the damage already done. This is an illegal state.
                // The tiles mark an invalid ship if the error is suppressed.
                throw new IllegalStateException("Tile at " + pos + " already contains a ship, can't add here. INVALID STATE CAUSED. DO NOT SUPPRESS THIS ERROR.");
            }
            tiles[pos.x][pos.y].containedShip = ship.getId();
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Grid ").append(name).append(":\n");
        
        for (TileData[] row : tiles) {
            for (TileData tile : row) {
                if (tile.isShot) {
                    result.append(tile.containedShip != null ? "X" : "O");
                } else {
                    result.append(tile.containedShip != null ? "S" : ".");
                }
                result.append(" ");
            }
            result.append("\n");
        }
        
        return result.toString();
    }
}