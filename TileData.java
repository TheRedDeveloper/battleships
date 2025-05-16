import java.util.UUID;

public class TileData {
    public boolean isHit;
    public UUID containedShip;
    
    public TileData(boolean isShot, UUID containedShip) {
        this.isHit = isShot;
        this.containedShip = containedShip;
    }
}
