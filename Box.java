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

    @Override
    public String toString() {
        return "Box[" + sx + "x" + sy + "]";
    }
}