public class Tile {
    private TileData data;
    private Position position;

    public Tile(TileData data, Position position) {
        this.data = data;
        this.position = position;
    }

    public boolean isOccupied() {
        return data.containedShip != null;
    }
}
