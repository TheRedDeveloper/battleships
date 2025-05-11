public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public Direction rotated(RotationDirection rotation) {
        switch (rotation) {
            case CLOCKWISE:
                return switch (this) {
                    case UP -> RIGHT;
                    case DOWN -> LEFT;
                    case LEFT -> UP;
                    case RIGHT -> DOWN;
                };
            case COUNTER_CLOCKWISE:
                return switch (this) {
                    case UP -> LEFT;
                    case DOWN -> RIGHT;
                    case LEFT -> DOWN;
                    case RIGHT -> UP;
                };
            case FLIP:
                return switch (this) {
                    case UP -> DOWN;
                    case DOWN -> UP;
                    case LEFT -> RIGHT;
                    case RIGHT -> LEFT;
                };
            case NONE:
                return this;
            default:
                throw new IllegalArgumentException("Invalid rotation direction: " + rotation);
        }
    }
}
