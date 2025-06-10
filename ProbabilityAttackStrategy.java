import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ProbabilityAttackStrategy implements AttackStrategy {
    public static Instant lastLog = Instant.ofEpochSecond(0);
    public static int timesRan = 0;
    private static Instant startTime;

    private final AtomicReference<AttackStrategyStatus> status = new AtomicReference<>(new AttackStrategyStatus(false, "", null));
    private static class FastRandom {
        private long seed;
        
        public FastRandom() {
            this.seed = System.nanoTime();
        }
        
        public FastRandom(long seed) {
            this.seed = seed;
        }
        
        public int nextInt(int bound) {
            seed ^= (seed << 21);
            seed ^= (seed >>> 35);
            seed ^= (seed << 4);
            return (int)((seed & 0x7FFFFFFF) % bound);
        }
    }
    private static final FastRandom FAST_RANDOM = new FastRandom();
    
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
        status.set(new AttackStrategyStatus(true, "Generating belief states...", null));
        long beliefStateCount = generateHitCountWithBeliefs(1, opponentGrid, realState, shipTypesLeft, hitCount);
        timesRan++;
        status.set(new AttackStrategyStatus(false, "", null));
        if (beliefStateCount == 0) {
            Game.LOGGER.info("No belief states generated, returning random position.");
            return new Position(Game.RANDOM.nextInt(10), Game.RANDOM.nextInt(10));
        }
        // Game.LOGGER.info("Hit count "+ timesRan + ":\n" + formatLong10x10Beautiful(hitCount));
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

        double[][] certaintyHeatmap = new double[10][10];

        while (beliefStateCount < 10000000 && Duration.between(startTime, Instant.now()).toSeconds() < seconds) {
            ShipType shipType = shipTypesLeft.get(0);
            List<List<Position>> occupiedRelativePositionsList = occupiedRelativePositionsForAllRotationsByShipType.get(shipType);
            int occupiedRelativePositionsListSize = occupiedRelativePositionsList.size();
            boolean[] outBeliefGrid = new boolean[100];
            boolean found = false;
            while (!found) {
                outBeliefGrid = beliefGrid.clone();
                if(monteCarloTreeSearchMakeValidBeliefGrid(
                    FAST_RANDOM.nextInt(10),
                    FAST_RANDOM.nextInt(10),
                    occupiedRelativePositionsList.get(FAST_RANDOM.nextInt(occupiedRelativePositionsListSize)),
                    outBeliefGrid,
                    new ArrayList<>(shipTypesLeft),
                    hitPositions,
                    hasKnownShip,
                    isMiss,
                    occupiedRelativePositionsForAllRotationsByShipType
                ) == GridValidationResult.VALID) {
                    found = true;
                }
            }
            for (int j = 0; j < 100; j++) {
                if (outBeliefGrid[j]) {
                    outHitCount[j / 10][j % 10]++;
                }
            }
            beliefStateCount++;
            if (beliefStateCount % 10000 == 0 && Duration.between(lastLog, Instant.now()).toMillis() > 200) {
                lastLog = Instant.now();
                // StringBuilder gridStr = new StringBuilder("\n");
                // for (int i = 0; i < 10; i++) {
                //     for (int j = 0; j < 10; j++) {
                //         gridStr.append(outBeliefGrid[j * 10 + i] ? "1 " : "0 ");
                //     }
                //     gridStr.append("\n");
                // }
                long milis = Duration.between(startTime, Instant.now()).toMillis();
                long perSecond = beliefStateCount * 1000 / milis;
                // Game.LOGGER.info("Generated " + beliefStateCount + " belief states in " + milis + " ms (" + perSecond + "/s)" + gridStr);
                
                for (int x = 0; x < 10; x++) {
                    for (int y = 0; y < 10; y++) {
                        certaintyHeatmap[x][y] = Math.min(1.0, Math.max(0.0, (double)outHitCount[x][y] / beliefStateCount));
                    }
                }
                
                status.set(new AttackStrategyStatus(
                    true,
                    beliefStateCount + " possibilities accounted (" + perSecond + "/s)",
                    certaintyHeatmap
                ));
            }
        }
        return beliefStateCount;
    }

    private GridValidationResult monteCarloTreeSearchMakeValidBeliefGrid(
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
    ) {
        boolean allHits = true;
        for (Position occupiedRelativePosition : occupiedRelativePositions) {
            if (col + occupiedRelativePosition.x < 0 || col + occupiedRelativePosition.x >= 10 ||
                row + occupiedRelativePosition.y < 0 || row + occupiedRelativePosition.y >= 10 || 
                outBeliefGrid[(col + occupiedRelativePosition.x) * 10 + (row + occupiedRelativePosition.y)] ||
                isMiss[col + occupiedRelativePosition.x][row + occupiedRelativePosition.y]) {
                return GridValidationResult.INVALID;
            }
            if (allHits && !hasKnownShip[col + occupiedRelativePosition.x][row + occupiedRelativePosition.y]) {
                allHits = false;
            }
        }
        if (allHits) {
            return GridValidationResult.INVALID; // return false cuz this ship would already be sunk
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
                        return GridValidationResult.BACK_TO_THE_START; // We need to go back to generateHitCountWithBeliefs
                    }
                } else {
                    if (outBeliefGrid[idx]) {
                        return GridValidationResult.BACK_TO_THE_START; // We need to go back to generateHitCountWithBeliefs
                    }
                }
            }
        } else {
            // Go next ship, random rotation, random position
            List<List<Position>> rotations = occupiedRelativePositionsForAllRotationsByShipType.get(outShipTypesLeft.get(0));
            GridValidationResult result;
            do {
                result = monteCarloTreeSearchMakeValidBeliefGrid(
                    FAST_RANDOM.nextInt(10),  // Using specialized method
                    FAST_RANDOM.nextInt(10),  // Using specialized method
                    rotations.get(FAST_RANDOM.nextInt(rotations.size())),
                    outBeliefGrid,
                    outShipTypesLeft,
                    hitPositions,
                    hasKnownShip,
                    isMiss,
                    occupiedRelativePositionsForAllRotationsByShipType
                );
                if (result == GridValidationResult.BACK_TO_THE_START) {
                    return GridValidationResult.BACK_TO_THE_START; // This will be caught in generateHitCountWithBeliefs
                }
            } while (result == GridValidationResult.INVALID);
        }
        return GridValidationResult.VALID;
    }

    enum GridValidationResult {
        VALID,
        INVALID,
        BACK_TO_THE_START
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

    @Override
    public AttackStrategyStatus getStatus() {
        return status.get();
    }
}
