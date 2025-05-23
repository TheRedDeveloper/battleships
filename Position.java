/** A coordinate in 2D space with x and y coordinates.
 * 
 *  If you need to represent height and width, use {@link Box} instead.
 * 
 *  This class is important for ensuring code standards. */
public class Position {
    public int x;
    public int y;
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Position add(Position other) {
        return new Position(x + other.x, y + other.y);
    }

    public Position[] getAdjacentPositions() {
        return new Position[] {
            new Position(x, y - 1),  // Up
            new Position(x + 1, y),  // Right
            new Position(x, y + 1),  // Down
            new Position(x - 1, y)   // Left
        };
    }
    
    // For equality comparison
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }
    
    @Override
    public String toString() {
        return "Position[" + x + "," + y + "]";
    }
}
