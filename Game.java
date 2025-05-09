

public class Game {
    private static GameState state = new GameState(StartMenuMode.getInstance());
    private static boolean isRunning;
    private static AsciiDisplay display;
    private static InputManager inputManager;

    public static void play() {
        try {
            System.out.println("Starting game...");
            isRunning = true;
            
            System.out.println("Initializing display...");
            inputManager = new InputManager();
            display = new AsciiDisplay(inputManager);
            
            if (display != null) {
                System.out.println("Showing display window...");
                display.show();
                
                System.out.println("Waiting for UI initialization...");
                Thread.sleep(1000);
                
                StartMenuMode.getInstance().enter();
                
                System.out.println("Entering game loop...");
                while (isRunning) {
                    state.getMode().render(state, display);
                    state = state.getMode().update(state, inputManager);
                    
                    Thread.sleep(50);
                }
            } else {
                System.err.println("Failed to initialize display!");
            }
        } catch (Exception e) {
            System.err.println("Error in game loop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void stop() {
        System.out.println("Stopping game...");
        isRunning = false;
    }
    
    public static void main(String[] args) {
        System.out.println("Game main method called");
        
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set look and feel: " + e.getMessage());
        }
        
        Game.play();
    }
}
