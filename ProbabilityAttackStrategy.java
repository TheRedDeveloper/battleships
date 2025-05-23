import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProbabilityAttackStrategy implements AttackStrategy {
    public static int _debugBeliefStateCount = 0;

    @Override
    public Position generateAttackPosition(Grid opponentGrid) {
        // TODO
        Game.LOGGER.info("Generating attack position...");
        int[][] hitCount = new int[10][10];
        boolean[][] realState = new boolean[10][10];
        List<ShipType> shipTypesLeft = new ArrayList<>();
        BuildMode.totalShipTypeCounts.forEach((shipType, count) -> {
            for (int i = 0; i < count; i++) {
                shipTypesLeft.add(shipType);
            }
        });
        opponentGrid.getSunkShips().forEach(ship -> {
            for (Position pos : ship.getOccupiedPositions()) {
                realState[pos.x][pos.y] = true;
            }
            shipTypesLeft.remove(ship.getType());
        });
        Game.LOGGER.info("Generating belief states...");
        for (boolean[][] beliefGrid : generateBeliefStates(opponentGrid, realState, shipTypesLeft)) {
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    if (beliefGrid[row][col]) {
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
    private List<boolean[][]> generateBeliefStates(Grid opponentGrid, boolean[][] beliefGrid, List<ShipType> shipTypesLeft) {
        if (shipTypesLeft.isEmpty()) {
            if (opponentGrid.getHitTiles().stream().allMatch(tile ->
                tile.data.containedShip != null ?
                    beliefGrid[tile.position.x][tile.position.y]
                    : !beliefGrid[tile.position.x][tile.position.y])) {
                List<boolean[][]> result = new ArrayList<>();
                result.add(beliefGrid); // Why Java, why?
                _debugBeliefStateCount++;
                if (_debugBeliefStateCount % 1000 == 0) {
                    StringBuilder gridStr = new StringBuilder("\n");
                    for (int i = 0; i < beliefGrid.length; i++) {
                        for (int j = 0; j < beliefGrid[i].length; j++) {
                            gridStr.append(beliefGrid[j][i] ? "1 " : "0 ");
                        }
                        gridStr.append("\n");
                    }
                    Game.LOGGER.info("Generated " + _debugBeliefStateCount + " belief states" + gridStr);
                }
                return result;
            } else {
                return new ArrayList<>();
            }
        } 
        List<boolean[][]> beliefStates = new ArrayList<>();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                Position pos = new Position(row, col);
                ShipBox shipBox = Ship.boxByType.get(shipTypesLeft.get(0));
                Direction[] uniqueDirections = shipBox.getUniqueDirections();
                for (Direction direction : uniqueDirections) {
                    ShipBox rotation = shipBox.inDirection(direction);
                    // TODO: This needs a lot of optimization
                    boolean valid = rotation.getOccupiedRelativePositions().stream()
                            .allMatch(occupiedPosition -> {
                                Position absolutePosition = occupiedPosition.add(pos);
                                if (!opponentGrid.isInBounds(absolutePosition)) return false;
                                boolean overlap = beliefGrid[absolutePosition.x][absolutePosition.y];
                                if (overlap) {
                                    return false;
                                } else if (!opponentGrid.getTile(absolutePosition).data.isHit) {
                                    return true; // It's unknown
                                } else {
                                    return opponentGrid.getShipAt(absolutePosition) != null;
                                }
                            });
                    if (valid) {
                        boolean[][] newBeliefGrid = new boolean[10][10];
                        List<Position> occupiedPositions = rotation.getOccupiedRelativePositions();
                        for (int i = 0; i < 10; i++) {
                            for (int j = 0; j < 10; j++) {
                                if (occupiedPositions.contains(new Position(i - pos.x, j - pos.y))) {
                                    newBeliefGrid[i][j] = true;
                                } else {
                                    newBeliefGrid[i][j] = beliefGrid[i][j];
                                }
                            }
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
