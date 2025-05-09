public enum RotationDirection {
    CLOCKWISE, COUNTER_CLOCKWISE, FLIP, NONE;

    public static RotationDirection fromToDirection(Direction from, Direction to) {
        return switch (from) {
            case UP -> switch (to) {
                case UP -> NONE;
                case DOWN -> FLIP;
                case LEFT -> COUNTER_CLOCKWISE;
                case RIGHT -> CLOCKWISE;
            };
            case DOWN -> switch (to) {
                case UP -> FLIP;
                case DOWN -> NONE;
                case LEFT -> CLOCKWISE;
                case RIGHT -> COUNTER_CLOCKWISE;
            };
            case LEFT -> switch (to) {
                case UP -> CLOCKWISE;
                case DOWN -> COUNTER_CLOCKWISE;
                case LEFT -> NONE;
                case RIGHT -> FLIP;
            };
            case RIGHT -> switch (to) {
                case UP -> COUNTER_CLOCKWISE;
                case DOWN -> CLOCKWISE;
                case LEFT -> FLIP;
                case RIGHT -> NONE;
            };
        };
    }
}