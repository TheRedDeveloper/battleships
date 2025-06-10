import java.util.ArrayList;
import java.util.List;

public class CheatAttackStrategy implements AttackStrategy {
    @Override
    public Position generateAttackPosition(Grid opponentGrid) {
        List<Tile> unhitOccupiedTiles = new ArrayList<>();
        for (Tile tile : opponentGrid.getTiles()) {
            if (!tile.data.isHit && tile.isOccupied()) {
                unhitOccupiedTiles.add(tile);
            }
        }
        if (!unhitOccupiedTiles.isEmpty()) {
            return unhitOccupiedTiles.get(Game.RANDOM.nextInt(unhitOccupiedTiles.size())).position;
        } else {
            throw new IllegalStateException("No hitting attack positions available. We have already won.");
        }
    }

    @Override
    public AttackStrategyStatus getStatus() {
        return null; // Its instantaneous
    }
}
