import java.util.UUID;

public class TileData {
    public boolean isShot;
    public UUID containedShip;
    
    public TileData(boolean isShot, UUID containedShip) {
        this.isShot = isShot;
        this.containedShip = containedShip;
    }
}
