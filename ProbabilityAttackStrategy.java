import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ProbabilityAttackStrategy implements AttackStrategy {
    public static long _debugBeliefStateCount = 0;
    private static Instant startTime;

    @Override
    public Position generateAttackPosition(Grid opponentGrid) {
        Game.LOGGER.info("Generating attack position...");
        startTime = Instant.now();
        long[][] hitCount = new long[10][10];
        boolean[] realState = new boolean[100];
        List<ShipType> shipTypesLeft = new ArrayList<>();
        BuildMode.totalShipTypeCounts.forEach((shipType, count) -> {
            for (int i = 0; i < count; i++) {
                shipTypesLeft.add(shipType);
            }
        });
        opponentGrid.getSunkShips().forEach(ship -> {
            for (Position pos : ship.getOccupiedPositions()) {
                realState[pos.x * 10 + pos.y] = true;
            }
            shipTypesLeft.remove(ship.getType());
        });
        Game.LOGGER.info("Generating belief states...");
        long beliefCount = beliefCount(opponentGrid, realState, shipTypesLeft, hitCount);
        Game.LOGGER.info(Arrays.deepToString(hitCount));
        Game.LOGGER.info("Total belief states generated: " + beliefCount);
        return new Position(0, 0);
    }


    private long beliefCount(Grid opponentGrid, boolean[] beliefGrid, List<ShipType> shipTypesLeft, long[][] outHitCount) {
        boolean[][] isMiss = new boolean[10][10];
        for (Tile tile : opponentGrid.getTiles()) {
            isMiss[tile.position.x][tile.position.y] = tile.data.isHit && tile.data.containedShip == null;
        }
        List<Position> hitPositions = opponentGrid.getHitTiles().stream().map(tile -> tile.position).toList();
        boolean[][] hasShip = new boolean[10][10];
        for (Tile tile : opponentGrid.getTiles()) {
            if (tile.data.containedShip != null) {
                hasShip[tile.position.x][tile.position.y] = true;
            }
        }

        // ShipType -> Rotation -> List of occupied positions
        Map<ShipType, List<List<Position>>> occupiedRelativePositionsForAllRotationsByShipType = new EnumMap<>(ShipType.class);
        for (ShipType shipType : shipTypesLeft) {
            ShipBox shipBox = Ship.boxByType.get(shipType);
            List<List<Position>> rotationPositions = new ArrayList<>();
            for (Direction direction : shipBox.getUniqueDirections()) {
                rotationPositions.add(shipBox.inDirection(direction).getOccupiedRelativePositions());
            }
            occupiedRelativePositionsForAllRotationsByShipType.put(shipType, rotationPositions);
        }

        return beliefCountRecursion(isMiss, hitPositions, hasShip, occupiedRelativePositionsForAllRotationsByShipType, opponentGrid, beliefGrid, shipTypesLeft, outHitCount);
    }
    private long beliefCountRecursion(boolean[][] isMiss, List<Position> hitPositions, boolean[][] hasShip, Map<ShipType, List<List<Position>>> occupiedRelativePositionsForAllRotationsByShipType, Grid opponentGrid, boolean[] beliefGrid, List<ShipType> shipTypesLeft, long[][] outHitCount) {
        if (shipTypesLeft.isEmpty()) {
            boolean allMatch = true;
            for (Position pos : hitPositions) {
                int idx = pos.x * 10 + pos.y;
                if (hasShip[pos.x][pos.y]) {
                    if (!beliefGrid[idx]) {
                        allMatch = false;
                        break;
                    }
                } else {
                    if (beliefGrid[idx]) {
                        allMatch = false;
                        break;
                    }
                }
            }
            if (allMatch) {
                _debugBeliefStateCount++;
                if (_debugBeliefStateCount % 1000000 == 0) {
                    StringBuilder gridStr = new StringBuilder("\n");
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            gridStr.append(beliefGrid[j * 10 + i] ? "1 " : "0 ");
                        }
                        gridStr.append("\n");
                    }
                    long milis = Duration.between(startTime, Instant.now()).toMillis();
                    long perSecond = _debugBeliefStateCount * 1000 / milis;
                    Game.LOGGER.info("Generated " + _debugBeliefStateCount + " belief states in " + milis + " ms (" + perSecond + "/s)" + gridStr);
                }
                for (int col = 0; col < 10; col++) {
                    for (int row = 0; row < 10; row++) {
                        if (beliefGrid[row * 10 + col]) {
                            outHitCount[row][col]++;
                        }
                    }
                }
                return 1;
            } else {
                return 0;
            }
        } else {
            int beliefCount = 0;
            ShipBox shipBox = Ship.boxByType.get(shipTypesLeft.get(0));
            for (int col = 0; col < 10; col++) {
                for (int row = 0; row < 10; row++) {
                    for (List<Position> occupiedRelativePositions : occupiedRelativePositionsForAllRotationsByShipType.get(shipTypesLeft.get(0))) {
                        boolean valid = true;
                        for (Position occupiedRelativePosition : occupiedRelativePositions) {
                            if (col + occupiedRelativePosition.x < 0 || col + occupiedRelativePosition.x >= 10 ||
                                row + occupiedRelativePosition.y < 0 || row + occupiedRelativePosition.y >= 10 || 
                                beliefGrid[(col + occupiedRelativePosition.x) * 10 + (row + occupiedRelativePosition.y)] ||
                                isMiss[col + occupiedRelativePosition.x][row + occupiedRelativePosition.y]) {
                                valid = false;
                                break;
                            }
                        }
                        if (valid) {
                            boolean[] newBeliefGrid = beliefGrid.clone(); 
                            for (Position occupiedRelativePosition : occupiedRelativePositions) {
                                newBeliefGrid[(occupiedRelativePosition.x + col) * 10 + occupiedRelativePosition.y + row] = true;
                            }
                            List<ShipType> newShipTypesLeft = new ArrayList<>(shipTypesLeft);
                            newShipTypesLeft.remove(0);
                            beliefCount += beliefCountRecursion(isMiss, hitPositions, hasShip, occupiedRelativePositionsForAllRotationsByShipType, opponentGrid, newBeliefGrid, newShipTypesLeft, outHitCount);
                        }
                    }
                }
            }
            return beliefCount;
        }
    }
}
