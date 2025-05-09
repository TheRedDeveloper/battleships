public class GameState {
    private GameMode mode;
    private Grid[] grids;

    public GameState(GameMode mode) {
        this.mode = mode;
        grids = new Grid[2];
    }

    public Tile getTile(int grid, int x, int y) {
        return grids[grid].getTile(x, y);
    }

    public void setTile(int grid, int x, int y, TileData tileData) {
        grids[grid].setTile(x, y, tileData);
    }

    public void setMode(GameMode mode) {
        this.mode.exit();
        this.mode = mode;
        mode.enter();
    }

    public GameMode getMode() {
        if (mode == null) {
            throw new IllegalStateException("Game mode is not set. Call setMode() first.");
        }
        return mode;
    }
}
