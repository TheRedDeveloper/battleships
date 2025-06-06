import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ProbabilityAttackStrategy implements AttackStrategy {
    public static Instant _debugLastLog = Instant.ofEpochSecond(0);
    public static int _debugTimesRan = 0;
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
        Game.LOGGER.info(Arrays.deepToString(hitCount));
        long beliefStateCount = generateHitCountWithBeliefs(10, opponentGrid, realState, shipTypesLeft, hitCount);
        _debugTimesRan++;
        if (beliefStateCount == 0) {
            Game.LOGGER.info("No belief states generated, returning random position.");
            return new Position(Game.RANDOM.nextInt(10), Game.RANDOM.nextInt(10));
        }
        Game.LOGGER.info("Hit count "+ _debugTimesRan + ":\n" + formatLong10x10Beautiful(hitCount));
        return getHighestScorePosition(hitCount, opponentGrid);
    }

    private Position getHighestScorePosition(long[][] hitCount, Grid opponentGrid) {
        int maxX = 0;
        int maxY = 0;
        long maxScore = hitCount[0][0];
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                if (hitCount[x][y] > maxScore && !opponentGrid.getTile(x, y).data.isHit) {
                    maxScore = hitCount[x][y];
                    maxX = x;
                    maxY = y;
                }
            }
        }
        return new Position(maxX, maxY);
    }

    private long generateHitCountWithBeliefs(int seconds, Grid opponentGrid, boolean[] beliefGrid, List<ShipType> shipTypesLeft, long[][] outHitCount) {
        long beliefStateCount = 0;
        boolean[][] isMiss = new boolean[10][10];
        for (Tile tile : opponentGrid.getTiles()) {
            isMiss[tile.position.x][tile.position.y] = tile.data.isHit && tile.data.containedShip == null;
        }
        boolean[][] hasKnownShip = new boolean[10][10];
        List<Position> hitPositions = opponentGrid.getHitTiles().stream().map(tile -> tile.position).toList();
        Game.LOGGER.info("Hit positions: " + hitPositions.toString());
        for (Tile tile : opponentGrid.getTiles()) {
            if (tile.data.containedShip != null && tile.data.isHit) {
                hasKnownShip[tile.position.x][tile.position.y] = true;
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

        while (beliefStateCount < 10000000 && Duration.between(startTime, Instant.now()).toSeconds() < seconds) {
            ShipType shipType = shipTypesLeft.get(0);
            List<List<Position>> occupiedRelativePositionsList = occupiedRelativePositionsForAllRotationsByShipType.get(shipType);
            int occupiedRelativePositionsListSize = occupiedRelativePositionsList.size();
            boolean[] outBeliefGrid = new boolean[100];
            boolean found = false;
            while (!found) {
                outBeliefGrid = beliefGrid.clone();
                try {
                    found = monteCarloTreeSearchMakeValidBeliefGrid(
                        Game.RANDOM.nextInt(10),
                        Game.RANDOM.nextInt(10),
                        occupiedRelativePositionsList.get(Game.RANDOM.nextInt(occupiedRelativePositionsListSize)),
                        outBeliefGrid,
                        new ArrayList<>(shipTypesLeft),
                        hitPositions,
                        hasKnownShip,
                        isMiss,
                        occupiedRelativePositionsForAllRotationsByShipType
                    );
                } catch (AbusingJavaExceptionsBullshitExeception e) {
                    found = false;
                }
            }
            for (int j = 0; j < 100; j++) {
                if (outBeliefGrid[j]) {
                    outHitCount[j / 10][j % 10]++;
                }
            }
            beliefStateCount++;
            if (beliefStateCount % 10000 == 0 && Duration.between(_debugLastLog, Instant.now()).toMillis() > 1000) {
                _debugLastLog = Instant.now();
                StringBuilder gridStr = new StringBuilder("\n");
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        gridStr.append(outBeliefGrid[j * 10 + i] ? "1 " : "0 ");
                    }
                    gridStr.append("\n");
                }
                long milis = Duration.between(startTime, Instant.now()).toMillis();
                long perSecond = beliefStateCount * 1000 / milis;
                Game.LOGGER.info("Generated " + beliefStateCount + " belief states in " + milis + " ms (" + perSecond + "/s)" + gridStr);
            }
        }
        return beliefStateCount;
    }

    private boolean monteCarloTreeSearchMakeValidBeliefGrid(
        int col,
        int row,
        List<Position> occupiedRelativePositions,
        boolean[] outBeliefGrid,
        List<ShipType> outShipTypesLeft,
        List<Position> hitPositions,
        boolean[][] hasKnownShip,
        boolean[][] isMiss,
        Map<ShipType,
        List<List<Position>>> occupiedRelativePositionsForAllRotationsByShipType
    ) throws AbusingJavaExceptionsBullshitExeception {
        boolean allHits = true;
        for (Position occupiedRelativePosition : occupiedRelativePositions) {
            if (col + occupiedRelativePosition.x < 0 || col + occupiedRelativePosition.x >= 10 ||
                row + occupiedRelativePosition.y < 0 || row + occupiedRelativePosition.y >= 10 || 
                outBeliefGrid[(col + occupiedRelativePosition.x) * 10 + (row + occupiedRelativePosition.y)] ||
                isMiss[col + occupiedRelativePosition.x][row + occupiedRelativePosition.y]) {
                return false;
            }
            if (allHits && !hasKnownShip[col + occupiedRelativePosition.x][row + occupiedRelativePosition.y]) {
                allHits = false;
            }
        }
        if (allHits) {
            return false; // return false cuz this ship would already be sunk
        }
        for (Position occupiedRelativePosition : occupiedRelativePositions) {
            outBeliefGrid[(occupiedRelativePosition.x + col) * 10 + occupiedRelativePosition.y + row] = true;
        }
        outShipTypesLeft.remove(0);
        if (outShipTypesLeft.isEmpty()) {
            for (Position pos : hitPositions) {
                int idx = pos.x * 10 + pos.y;
                if (hasKnownShip[pos.x][pos.y]) {
                    if (!outBeliefGrid[idx]) {
                        throw new AbusingJavaExceptionsBullshitExeception();
                    }
                } else {
                    if (outBeliefGrid[idx]) {
                        throw new AbusingJavaExceptionsBullshitExeception();
                    }
                }
            }
        } else {
            // Go next ship, random rotation, random position
            List<List<Position>> rotations = occupiedRelativePositionsForAllRotationsByShipType.get(outShipTypesLeft.get(0));
            while(
                !monteCarloTreeSearchMakeValidBeliefGrid(
                    Game.RANDOM.nextInt(10),
                    Game.RANDOM.nextInt(10),
                    rotations.get(Game.RANDOM.nextInt(rotations.size())),
                    outBeliefGrid,
                    outShipTypesLeft,
                    hitPositions,
                    hasKnownShip,
                    isMiss,
                    occupiedRelativePositionsForAllRotationsByShipType
                )
            ) {
                // Try again
            }
        }
        return true;
    }

    public class AbusingJavaExceptionsBullshitExeception extends RuntimeException {
        public AbusingJavaExceptionsBullshitExeception() {
            super("This exception is thrown to abuse Java's exception handling for control flow.");
        }
    }

    public static String formatLong10x10Beautiful(long[][] long10x10) {
        StringBuilder sb = new StringBuilder();
        
        // Convert to String[11][11] with labels
        String[][] stringArray = new String[11][11];
        
        // First row: "", "0", "1", "2", ..., "9"
        stringArray[0][0] = "";
        for (int i = 0; i < 10; i++) {
            stringArray[0][i+1] = String.valueOf(i);
        }
        
        // Remaining rows: "0", "1", etc. + data
        for (int row = 0; row < 10; row++) {
            stringArray[row+1][0] = String.valueOf(row); // Row label
            for (int col = 0; col < 10; col++) {
            // long10x10[col] because first index is column
            stringArray[row+1][col+1] = String.valueOf(long10x10[col][row]);
            }
        }
        
        // Find max length for each column
        int[] colLen = new int[11];
        for (int col = 0; col < 11; col++) {
            for (int row = 0; row < 11; row++) {
                colLen[col] = Math.max(colLen[col], stringArray[row][col].length());
            }
        }
        
        // Build string with proper spacing
        for (int row = 0; row < 11; row++) {
            for (int col = 0; col < 11; col++) {
                String value = stringArray[row][col];
                int spacesNeeded = colLen[col] - value.length();
                
                // Add spaces before the value (right-align)
                for (int s = 0; s < spacesNeeded; s++) {
                    sb.append(" ");
                }
                sb.append(value);
                
                // Add space after each column (except the last one)
                if (col < 10) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }

    double[][] getCertainty(){
        // clamp(0,1,hitCount / beliefStateCount / ((startTime - time) / 10s))
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
