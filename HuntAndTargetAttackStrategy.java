import java.util.ArrayList;
import java.util.List;

public class HuntAndTargetAttackStrategy implements AttackStrategy {
    @Override
    public Position generateAttackPosition(Grid opponentGrid) {
        List<Position> toBeShot = new ArrayList<>();

        boolean hitsRevealed = opponentGrid.getTiles().stream()
            .anyMatch(tile -> tile.data.isHit && opponentGrid.getShipAt(tile.position) != null 
                && !opponentGrid.getShipAt(tile.position).isSunk());

        if (hitsRevealed) {
            // the four unhit adjacent tiles for each hit tile
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    Tile tile = opponentGrid.getTile(x, y);
                    Ship ship = opponentGrid.getShipAt(tile.position);
                    if (tile.data.isHit && ship != null && !ship.isSunk()) {
                        for (Position adjacent : tile.position.getAdjacentPositions()) {
                            if (opponentGrid.isInBounds(adjacent)) {
                                Tile adjacentTile = opponentGrid.getTile(adjacent.x, adjacent.y);
                                if (!adjacentTile.data.isHit && !toBeShot.contains(adjacent)) {
                                    toBeShot.add(adjacent);
                                }
                            }
                        }
                    }
                }
            }
        } else {   
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    if (!opponentGrid.getTile(x, y).data.isHit) {
                        toBeShot.add(new Position(x, y));
                    }
                }
            }
        }
        if (toBeShot.isEmpty()) {
            throw new IllegalStateException("No available positions to attack");
        }
        
        return toBeShot.get(Game.RANDOM.nextInt(toBeShot.size()));
    }

    @Override
    public AttackStrategyStatus getStatus() {
        return null; // Its instantaneous
    }
}
