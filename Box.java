/** A locationless NxN box.
 * 
 *  Boxes with position information are called {@link Rect}.
 * 
 *  This class is important for ensuring code standards. */
public class Box {
    protected int sx;
    protected int sy;

    public int getWidth() {
        return sx;
    }
    public int getHeight() {
        return sy;
    }

    public Box(int sx, int sy) {
        this.sx = sx;
        this.sy = sy;
    }

    public int getArea() {
        return sx * sy;
    }

    public int getPerimeter() {
        return 2 * (sx + sy);
    }

    public int getDiagonal() {
        return (int) Math.sqrt(sx * sx + sy * sy);
    }

    public Position getRelativeCenter() {
        return new Position(sx / 2, sy / 2);
    }

    @Override
    public String toString() {
        return "Box[" + sx + "x" + sy + "]";
    }
}