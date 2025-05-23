import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[0].length) { throw new IllegalArgumentException("Tile coordinates out of bounds: (" + x + ", " + y + ")"); }
        return new Tile(tiles[x][y], new Position(x, y));
    }
    public Tile getTile(Position pos) { return getTile(pos.x, pos.y); }
    
    public void setTile(int x, int y, TileData tileData) {
        tiles[x][y] = tileData;
    }

    public List<Tile> getTiles() {
        List<Tile> tileList = new ArrayList<>();
        int x = 0;
        for (TileData[] row : tiles) {
            int y = 0;
            for (TileData tile : row) {
                tileList.add(new Tile(tile, new Position(x, y)));
                y++;
            }
            x++;
        }
        return tileList;
    }
    
    public void hitTile(int x, int y) {
        if (x < 0 || x >= tiles.length || y < 0 || y >= tiles[x].length) { throw new IllegalArgumentException("Invalid tile coordinates: (" + x + ", " + y + ")"); }
        if (tiles[x][y].isHit) { throw new IllegalStateException("Tile at (" + x + ", " + y + ") has already been hit."); }
        tiles[x][y].isHit = true;
        
        UUID shipId = tiles[x][y].containedShip;
        if (shipId != null) {
            Ship ship = ships.get(shipId);
            if (ship.getOccupiedPositions().stream().allMatch(pos -> tiles[pos.x][pos.y].isHit)) {
                ship.setSunk(true);
            }
        }
    }
    
    public String getName() { return name; }
    public Collection<Ship> getShips() { return ships.values(); }
    public Collection<Ship> getSunkShips() { return ships.values().stream().filter(Ship::isSunk).toList(); }

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

    public Ship getShipAt(Position pos) {
        return getShipAt(pos.x, pos.y);
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
    
    public boolean isLost() {
        return ships.values().stream().allMatch(Ship::isSunk);
    }
    
    public Collection<Tile> getHitTiles() {
        Collection<Tile> hitTiles = new ArrayList<>();
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                if (tiles[x][y].isHit) {
                    hitTiles.add(new Tile(tiles[x][y], new Position(x, y)));
                }
            }
        }
        return hitTiles;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Grid ").append(name).append(":\n");
        
        for (TileData[] row : tiles) {
            for (TileData tile : row) {
                if (tile.isHit) {
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
    
    public boolean isInBounds(Position pos) {
        return pos.x >= 0 && pos.x < tiles.length && pos.y >= 0 && pos.y < tiles[0].length;
    }
}