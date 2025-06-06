import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** a belief state model that explores all possible ship placements. */
public class OptimalAttackStrategy implements AttackStrategy {
    public static long _debugBeliefStateCount = 0;
    private static Instant startTime;

    @Override
    public Position generateAttackPosition(Grid opponentGrid) {
        Game.LOGGER.info("Generating attack position with optimal strategy...");
        startTime = Instant.now();
        long[][] hitCount = new long[10][10];
        boolean[] beliefGrid = new boolean[100];
        List<ShipType> shipTypesLeft = new ArrayList<>();
        
        BuildMode.totalShipTypeCounts.forEach((shipType, count) -> {
            for (int i = 0; i < count; i++) {
                shipTypesLeft.add(shipType);
            }
        });
        
        opponentGrid.getSunkShips().forEach(ship -> {
            for (Position pos : ship.getOccupiedPositions()) {
                beliefGrid[pos.x * 10 + pos.y] = true;
            }
            shipTypesLeft.remove(ship.getType());
        });
        
        Game.LOGGER.info("Generating belief states recursively...");
        _debugBeliefStateCount = 0;
        long totalBeliefStates = beliefCount(opponentGrid, beliefGrid, shipTypesLeft, hitCount);
        Game.LOGGER.info("Generated " + totalBeliefStates + " total belief states");
        
        return getHighestScorePosition(hitCount);
    }

    private Position getHighestScorePosition(long[][] hitCount) {
        int maxX = 0;
        int maxY = 0;
        long maxScore = hitCount[0][0];
        
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (hitCount[x][y] > maxScore) {
                    maxScore = hitCount[x][y];
                    maxX = x;
                    maxY = y;
                }
            }
        }
        
        return new Position(maxX, maxY);
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
            long beliefCount = 0;
            ShipType currentShipType = shipTypesLeft.get(0);
            List<ShipType> remainingShips = new ArrayList<>(shipTypesLeft);
            remainingShips.remove(0);
            
            for (int x = 0; x < 10; x++) {
                for (int y = 0; y < 10; y++) {
                    for (List<Position> occupiedRelativePositions : occupiedRelativePositionsForAllRotationsByShipType.get(currentShipType)) {
                        boolean valid = true;
                        
                        for (Position occupiedRelativePosition : occupiedRelativePositions) {
                            int newX = x + occupiedRelativePosition.x;
                            int newY = y + occupiedRelativePosition.y;
                            
                            if (newX < 0 || newX >= 10 || newY < 0 || newY >= 10 || 
                                beliefGrid[newX * 10 + newY] ||
                                isMiss[newX][newY]) {
                                valid = false;
                                break;
                            }
                        }
                        
                        if (valid) {
                            boolean[] newBeliefGrid = beliefGrid.clone();
                            for (Position occupiedRelativePosition : occupiedRelativePositions) {
                                int newX = x + occupiedRelativePosition.x;
                                int newY = y + occupiedRelativePosition.y;
                                newBeliefGrid[newX * 10 + newY] = true;
                            }
                            
                            beliefCount += beliefCountRecursion(
                                isMiss, 
                                hitPositions, 
                                hasShip,
                                occupiedRelativePositionsForAllRotationsByShipType,
                                opponentGrid, 
                                newBeliefGrid, 
                                remainingShips, 
                                outHitCount
                            );
                        }
                    }
                }
            }
            
            return beliefCount;
        }
    }
}
