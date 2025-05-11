import java.util.ArrayList;
import java.util.List;

/** The box of a shiptype on the grid */
public class ShipBox extends Box {
    private final boolean[][] atIsShip;
    private final String[] rowsToDisplay;
    /** Storing the original direction enables us to call {@link ShipBox#inDirection(Direction)} multiple times */
    private final Direction originalDirection;

    public ShipBox(int sx, int sy, boolean[][] atIsShip, String[] rowsToDisplay, Direction originalDirection) {
        super(sx, sy);
        this.atIsShip = atIsShip;
        this.rowsToDisplay = rowsToDisplay;
        this.originalDirection = originalDirection;
    }
    public boolean isShipAtRelativeCoordinates(int x, int y) {
        if (x < 0 || x >= sx || y < 0 || y >= sy) {
            return false;
        }
        return atIsShip[y][x];
    }

    /** List of positions occupied by the ShipBox */
    public List<Position> getOccupiedRelativePositions() {
        List<Position> positions = new ArrayList<>();
        for (int y = 0; y < sy; y++) {
            for (int x = 0; x < sx; x++) {
                if (atIsShip[y][x]) {
                    positions.add(new Position(x, y));
                }
            }
        }
        return positions;
    }
    
    /** Display the ship box at the given coordinates on the display */
    public void displayFromAbsoluteTopLeftOn(int x, int y, AsciiDisplay display) {
        for (int i = 0; i < rowsToDisplay.length; i++) {
            display.drawString(x, y + i, rowsToDisplay[i]);
        }
    }

    /** Get the ship box facing in the specified direction */
    public ShipBox inDirection(Direction direction) {
        RotationDirection rotation = RotationDirection.fromToDirection(originalDirection, direction);
        int newWidth = (rotation == RotationDirection.CLOCKWISE || rotation == RotationDirection.COUNTER_CLOCKWISE) ? sy : sx;
        int newHeight = (rotation == RotationDirection.CLOCKWISE || rotation == RotationDirection.COUNTER_CLOCKWISE) ? sx : sy;
        return new ShipBox(newWidth, newHeight, rotateBooleanMatrix(atIsShip, rotation), rotateStringArray(rowsToDisplay, rotation), direction);
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
    
    private String[] rotateStringArray(String[] array, RotationDirection direction) {
        if (array.length == 0) return new String[0];
        int height = array.length;
        int width = array[0].length();
        
        return switch (direction) {
            case CLOCKWISE -> {
                String[] rotated = new String[width];
                for (int j = 0; j < width; j++) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = height - 1; i >= 0; i--) {
                        sb.append(j < array[i].length() ? array[i].charAt(j) : ' ');
                    }
                    rotated[j] = sb.toString();
                }
                yield rotated;
            }
                
            case COUNTER_CLOCKWISE -> {
                String[] rotated = new String[width];
                for (int j = 0; j < width; j++) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < height; i++) {
                        sb.append((width - 1 - j) < array[i].length() ? array[i].charAt(width - 1 - j) : ' ');
                    }
                    rotated[j] = sb.toString();
                }
                yield rotated;
            }
                
            case FLIP -> {
                String[] rotated = new String[height];
                for (int i = 0; i < height; i++) {
                    StringBuilder sb = new StringBuilder(array[height - 1 - i]);
                    sb.reverse();
                    rotated[i] = sb.toString();
                }
                yield rotated;
            }

            case NONE -> array.clone();
                
            default -> throw new IllegalArgumentException("Unknown rotation direction");
        };
    }
}
