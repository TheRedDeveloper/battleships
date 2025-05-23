import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class ProbabilityAttackStrategy implements AttackStrategy {
    public static int _debugBeliefStateCount = 0;
    private static Instant startTime;

    @Override
    public Position generateAttackPosition(Grid opponentGrid) {
        Game.LOGGER.info("Generating attack position...");
        startTime = Instant.now();
        int[][] hitCount = new int[10][10];
        BitSet realState = new BitSet(100);
        List<ShipType> shipTypesLeft = new ArrayList<>();
        BuildMode.totalShipTypeCounts.forEach((shipType, count) -> {
            for (int i = 0; i < count; i++) {
                shipTypesLeft.add(shipType);
            }
        });
        opponentGrid.getSunkShips().forEach(ship -> {
            for (Position pos : ship.getOccupiedPositions()) {
                realState.set(pos.x * 10 + pos.y);
            }
            shipTypesLeft.remove(ship.getType());
        });
        Game.LOGGER.info("Generating belief states...");
        for (BitSet beliefGrid : generateBeliefStates(opponentGrid, realState, shipTypesLeft)) {
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    if (beliefGrid.get(row * 10 + col)) {
                        hitCount[row][col]++;
                    }
                }
            }
        }
        Game.LOGGER.info("Belief states generated!");
        Game.LOGGER.info(Arrays.deepToString(hitCount));
        return new Position(0, 0);
    }

    // TODO: This could probably benefit from iterative unfolding
    private List<BitSet> generateBeliefStates(Grid opponentGrid, BitSet beliefGrid, List<ShipType> shipTypesLeft) {
        if (shipTypesLeft.isEmpty()) {
            if (opponentGrid.getHitTiles().stream().allMatch(tile ->
                tile.data.containedShip != null ?
                    beliefGrid.get(tile.position.x * 10 + tile.position.y)
                    : !beliefGrid.get(tile.position.x * 10 + tile.position.y))) {
                        _debugBeliefStateCount++;
                        if (_debugBeliefStateCount % 1000 == 0) {
                            StringBuilder gridStr = new StringBuilder("\n");
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            gridStr.append(beliefGrid.get(j * 10 + i) ? "1 " : "0 ");
                        }
                        gridStr.append("\n");
                    }
                    long milis = Duration.between(startTime, Instant.now()).toMillis();
                    int perSecond = (int) ((long) _debugBeliefStateCount * 1000 / milis);
                    Game.LOGGER.info("Generated " + _debugBeliefStateCount + " belief states in " + milis + " ms (" + perSecond + "/s)" + gridStr);
                }
                List<BitSet> result = new ArrayList<>();
                result.add(beliefGrid);
                return result;
            } else {
                return new ArrayList<>();
            }
        } else {
            List<BitSet> beliefStates = new ArrayList<>();
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    Position pos = new Position(col, row);
                    ShipBox shipBox = Ship.boxByType.get(shipTypesLeft.get(0));
                    for (Direction direction : shipBox.getUniqueDirections()) {
                        ShipBox rotation = shipBox.inDirection(direction);
                        // TODO: This needs a lot of optimization
                        boolean valid = true;
                        for (Position occupiedPosition : rotation.getOccupiedRelativePositions()) {
                            Position absolutePosition = occupiedPosition.add(pos);
                            if (!opponentGrid.isInBounds(absolutePosition) || 
                                beliefGrid.get(absolutePosition.x * 10 + absolutePosition.y) ||
                                (opponentGrid.getTile(absolutePosition).data.isHit && 
                                 opponentGrid.getShipAt(absolutePosition) == null)) {
                                valid = false;
                                break;
                            }
                        }
                        if (valid) {
                            BitSet newBeliefGrid = (BitSet) beliefGrid.clone(); 
                            List<Position> occupiedPositions = rotation.getOccupiedRelativePositions();
                            for (Position occupiedPosition : occupiedPositions) {
                                newBeliefGrid.set((occupiedPosition.x + row) * 10 + occupiedPosition.y + col);
                            }
                            List<ShipType> newShipTypesLeft = new ArrayList<>(shipTypesLeft);
                            newShipTypesLeft.remove(0);
                            beliefStates.addAll(generateBeliefStates(opponentGrid, newBeliefGrid, newShipTypesLeft));
                        }
                    }
                }
            }
            return beliefStates;
        }
    }
}
