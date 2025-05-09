
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Grid {
    public TileData[][] tiles;
    public Map<UUID, Ship> ships;

    public Grid() {
        tiles = new TileData[10][10];
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                tiles[x][y] = new TileData(false, null);
            }
        }
        ships = new HashMap<>(); // I hate Java, I DON'T NEED HASHING BUT I HAVE TO USE IT
    }

    public Tile getTile(int x, int y) {
        return new Tile(tiles[x][y], new int[]{x, y});
    }

    public void setTile(int x, int y, TileData tileData) {
        tiles[x][y] = tileData;
    }
}