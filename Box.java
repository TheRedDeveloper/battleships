/* A locationless NxN box */
public class Box {
    public int sx;
    public int sy;

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
}