import java.util.ArrayList;
import java.util.List;

/** Strategy that selects attack positions randomly from unshot grid locations.
 *
 * @author GitHub Copilot */
public class RandomAttackStrategy implements AttackStrategy {
    @Override
    public Position generateAttackPosition(Grid opponentGrid) {
        List<Position> unshot = new ArrayList<>();
        
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (!opponentGrid.getTile(x, y).data.isHit) {
                    unshot.add(new Position(x, y));
                }
            }
        }
        
        if (unshot.isEmpty()) {
            throw new IllegalStateException("No available positions to attack");
        }
        
        return unshot.get(Game.RANDOM.nextInt(unshot.size()));
    }
}
