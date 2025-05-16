public class BotStrategy {
    private GridStrategy gridStrategy;
    private AttackStrategy attackStrategy;

    public BotStrategy(GridStrategy gridStrategy, AttackStrategy attackStrategy) {
        this.gridStrategy = gridStrategy;
        this.attackStrategy = attackStrategy;
    }

    Grid generateGrid() {
        return gridStrategy.generateGrid();
    }

    Position generateAttackPosition(Grid opponentGrid) {
        return attackStrategy.generateAttackPosition(opponentGrid);
    }
}
