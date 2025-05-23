import java.util.UUID;

public class TileData {
    public boolean isHit;
    public UUID containedShip;
    
    public TileData(boolean isShot, UUID containedShip) {
        this.isHit = isShot;
        this.containedShip = containedShip;
    }

    @Override
    public String toString() {
        // TileData[] if empty unhit
        // TileData[UUID] if ship is present
        // TileData[miss] if hit
        // TileData[UUID: hit] if hit, ship is present
        if (containedShip == null) {
            return isHit ? "TileData[miss]" : "TileData[]";
        } else {
            return isHit ? "TileData[" + containedShip + ": hit]" : "TileData[" + containedShip + "]";
        }
    }
}
