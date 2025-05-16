public class Tile {
    public TileData data;
    public Position position;

    public Tile(TileData data, Position position) {
        this.data = data;
        this.position = position;
    }

    public int getX() {
        return position.x;
    }
    public int getY() {
        return position.y;
    }

    public boolean isOccupied() {
        return data.containedShip != null;
    }
}
