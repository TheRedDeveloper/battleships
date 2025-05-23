import java.util.ArrayList;
import java.util.List;

/** The box of a shiptype on the grid @author RedDev */
public class ShipBox extends Box {
    private final boolean[][] occupationMatrix;
    /** Storing the original direction enables us to call {@link ShipBox#inDirection(Direction)} multiple times */
    private final Direction originalDirection;
    private final String color;

    public ShipBox(int sx, int sy, boolean[][] occupationMatrix, Direction originalDirection, String color) {
        super(sx, sy);
        if (occupationMatrix.length > sy || (occupationMatrix.length > 0 && occupationMatrix[0].length > sx)) {
            throw new IllegalArgumentException("occupationMatrix dimensions cannot exceed sx and sy");
        }
        this.occupationMatrix = occupationMatrix;
        this.originalDirection = originalDirection;
        this.color = color;
    }

    public boolean isShipAtRelativeCoordinates(int x, int y) {
        if (x < 0 || x >= sx || y < 0 || y >= sy) {
            return false;
        }
        return occupationMatrix[y][x];
    }

    /** List of positions occupied by the ShipBox */
    public List<Position> getOccupiedRelativePositions() {
        List<Position> positions = new ArrayList<>();
        for (int y = 0; y < sy; y++) {
            for (int x = 0; x < sx; x++) {
                if (occupationMatrix[y][x]) {
                    positions.add(new Position(x, y));
                }
            }
        }
        return positions;
    }
    
    /** Display the ship box as character at the given coordinates on the display with cellWidth*/
    public void displayFromAbsoluteTopLeftOn(int x, int y, AsciiDisplay display, char character, int cellWidth) {
        int dispY = 0;
        for (String row : makeStringRows(character, cellWidth)) {
            display.drawTransparentString(x, y + dispY, row, this.color);
            dispY++;
        }
    }

    /** Get the ship box facing in the specified direction */
    public ShipBox inDirection(Direction direction) {
        RotationDirection rotation = RotationDirection.fromToDirection(originalDirection, direction);
        int newWidth = (rotation == RotationDirection.CLOCKWISE || rotation == RotationDirection.COUNTER_CLOCKWISE) ? sy : sx;
        int newHeight = (rotation == RotationDirection.CLOCKWISE || rotation == RotationDirection.COUNTER_CLOCKWISE) ? sx : sy;
        return new ShipBox(newWidth, newHeight, rotateBooleanMatrix(occupationMatrix, rotation), direction, this.color);
    }

    /** Get the unique directions */
    public Direction[] getUniqueDirections() {
        if (sx == 1) {
            if (sy == 1) {
                return new Direction[] { originalDirection };
            } else {
                return new Direction[] { originalDirection, originalDirection.rotated(RotationDirection.CLOCKWISE) };
            }
        } else if (sy == 1) {
            return new Direction[] { originalDirection, originalDirection.rotated(RotationDirection.CLOCKWISE) };
        } else {
            return new Direction[] { originalDirection, originalDirection.rotated(RotationDirection.CLOCKWISE), originalDirection.rotated(RotationDirection.COUNTER_CLOCKWISE), originalDirection.rotated(RotationDirection.FLIP) };
        }
    }

    /** occupationMatrix = {{true, true}}, character = 1, cellWidth = 2 -> {"####"} */
    private String[] makeStringRows(char character, int cellWidth) {
        String[] rows = new String[occupationMatrix.length];
        for (int i = 0; i < occupationMatrix.length; i++) {
            StringBuilder rowBuilder = new StringBuilder();
            for (boolean isOccupied : occupationMatrix[i]) {
                if (isOccupied) {
                    rowBuilder.append(String.valueOf(character).repeat(cellWidth));
                } else {
                    rowBuilder.append(" ".repeat(cellWidth));
                }
            }
            rows[i] = rowBuilder.toString();
        }
        return rows;
    }
    
    private boolean[][] rotateBooleanMatrix(boolean[][] matrix, RotationDirection direction) {
        int height = matrix.length;
        int width = matrix[0].length;
        
        return switch (direction) {
            case CLOCKWISE -> {
                boolean[][] rotated = new boolean[width][height];
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        rotated[j][height - 1 - i] = matrix[i][j];
                    }
                }
                yield rotated;
            }
                
            case COUNTER_CLOCKWISE -> {
                boolean[][] rotated = new boolean[width][height];
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        rotated[width - 1 - j][i] = matrix[i][j];
                    }
                }
                yield rotated;
            }
                
            case FLIP -> {
                boolean[][] rotated = new boolean[height][width];
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        rotated[height - 1 - i][width - 1 - j] = matrix[i][j];
                    }
                }
                yield rotated;
            }

            case NONE -> matrix;                
            default -> throw new IllegalArgumentException("Unknown rotation direction");
        };
    }
}
