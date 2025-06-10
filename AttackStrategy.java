public interface AttackStrategy {
    Position generateAttackPosition(Grid opponentGrid);
    AttackStrategyStatus getStatus();

    public class AttackStrategyStatus {
        public boolean isGenerating;
        public String message;
        public double[][] certainty;

        public AttackStrategyStatus(boolean isGenerating, String message, double[][] certainty) {
            this.isGenerating = isGenerating;
            this.message = message;
            this.certainty = certainty;
        }
    }
}
