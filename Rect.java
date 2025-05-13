/** A rectangle defined by its position and size.
 *  
 *  If you need a locationless box, use {@link Box} instead.
 *  If you want to get the {@link Position}, use {@link Rect#asPosition()}.
 * 
 *  This class is important for ensuring code standards. */
public class Rect extends Box {
    protected int x;
    protected int y;

    public Rect(int x, int y, int sx, int sy) {
        super(sx, sy);
        this.x = x;
        this.y = y;
    }

    public Rect(int x, int y, Box box) { super(box.getWidth(), box.getHeight()); this.x = x; this.y = y; }
    public Rect(Position position, int sx, int sy) { super(sx, sy); this.x = position.x; this.y = position.y; }
    public Rect(Position position, Box box) { super(box.getWidth(), box.getHeight()); this.x = position.x; this.y = position.y; }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Position asPosition() {
        return new Position(x, y);
        // This is why traits in Rust are better.
        // YOU CAN BASICALLY EXTEND MULTIPLE CLASSES.
        // RUST DOES OOP BETTER THAN JAVA.
    }
    
    public Position getCenter() {
        return new Position(x + sx / 2, y + sy / 2);
    }

    @Override
    public String toString() {
        return "Rect[" + x + "," + y + " | " + sx + "x" + sy + "]";
    }
}