import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Represents a ship in the game.
 * 
 *  This class does not manage the look of a ship, for that use {@link Ship#getShipBox}. */
public class Ship {
    private ShipType type;
    private boolean isSunk;
    private int x;
    private int y;
    private UUID id; public UUID getId() { return id; }
    private Direction direction;

    protected static final Map<ShipType, ShipBox> boxByType = initializeBoxes();
    private static Map<ShipType, ShipBox> initializeBoxes() {
        EnumMap<ShipType, ShipBox> boxMap = new EnumMap<>(ShipType.class);
        
        boxMap.put(ShipType.BATTLESHIP4X1, new ShipBox(4, 1, 
            new boolean[][]{{true, true, true, true}}, 
            new String[]{"########"}, Direction.RIGHT, ANSI.BRIGHT_RED));
        
        boxMap.put(ShipType.CRUISER3X1, new ShipBox(3, 1, 
            new boolean[][]{{true, true, true}}, 
            new String[]{"######"}, Direction.RIGHT, ANSI.YELLOW));
        
        boxMap.put(ShipType.DESTROYER2X1, new ShipBox(2, 1, 
            new boolean[][]{{true, true}}, 
            new String[]{"####"}, Direction.RIGHT, ANSI.BRIGHT_GREEN));
        
        boxMap.put(ShipType.SUBMARINE1X1, new ShipBox(1, 1, 
            new boolean[][]{{true}}, 
            new String[]{"##"}, Direction.RIGHT, ANSI.WHITE));
        
        boxMap.put(ShipType.U, new ShipBox(3, 2, 
            new boolean[][]{
                {true, true, true},
                {true, false, true}
            },
            new String[]{
                "######",
                "##  ##"
            }, Direction.RIGHT, ANSI.CYAN));
        
        return boxMap;
    }

    public Ship(ShipType type, int x, int y, Direction direction) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.isSunk = false;
        this.id = UUID.randomUUID();
        this.direction = direction;
    }

    public ShipBox getShipBox() {
        return boxByType.get(type).inDirection(direction);
    }

    public List<Position> getOccupiedPositions() {
        return getShipBox().getOccupiedRelativePositions().stream()
            .map(pos -> pos.add(asRect().asPosition()))
            .toList();
    }

    public Rect asRect() { return new Rect(x, y, getShipBox()); }

    public void displayFromOrigin(int originX, int originY, AsciiDisplay display) {
        ShipBox box = Ship.boxByType.get(type);
        box = box.inDirection(direction);
        box.displayFromAbsoluteTopLeftOn(originX + x * 2, originY + y, display);
    }

    public ShipType getType() {
        return type;
    }

    public boolean isSunk() {
        return isSunk;
    }

    public void setSunk(boolean sunk) {
        isSunk = sunk;
    }
}
