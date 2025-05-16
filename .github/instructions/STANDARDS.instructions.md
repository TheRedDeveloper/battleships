---
applyTo: "**/*.java"
---
# Battleships Game Code Standards and Documentation

You should include this, when writing prod code with AI.

This guide itself is AI-generated, but the standards are mine.

## Code Style Standards

### Comments
- Class-level documentation uses JavaDoc style comments with two asterisks:
  ```java
  /** Represents a ship in the game.
   * 
   *  This class does not manage the look of a ship, for that use {@link Ship#getShipBox}. */
  ```
- Use `{@link ClassName#methodName}` when referencing other parts of the code
- **Important**: Comments should be explainers of why, not descriptors of what
  - **Good**: `// This is why traits in Rust are better.`
  - **Bad**: `// Initialize the x variable`
- Minimize unnecessary comments that just repeat what code already says and could be lying
- Only use comments when the code's purpose is not immediately obvious
- Use JavaDoc @author AIModelName to indicate something is AI-generated

### Code Compactness
- Keep code as compact as possible when clarity isn't compromised
- Avoid unnecessary verbosity in both code and comments
- If a one-liner clearly expresses intent, prefer it over multiple lines

### Naming Conventions
- Use clear, descriptive names that express intent
- Class names use PascalCase (e.g., `ShipBox`, `GameMode`)
- Variables and methods use camelCase (e.g., `isShot`, `getShipBox()`)
- Constants and enums use SCREAMING_SNAKE_CASE (not demonstrated in code)
- Avoid abbreviations unless universally understood
- Prefer meaningful variable names over cryptic short ones

### Display and UI Standards
- Use ANSI color constants from the `ANSI` class instead of direct color codes
- This ensures compatibility with terminal environments and keeps the UI consistent

### Coordinate System
- Always use `x` and `y` as variable names for coordinates, never `coordinates[0]` or array notation
- For consistency, `x` is horizontal and `y` is vertical
- 0 0 is the top-left corner
- Position, Box, and Rect classes enforce this coordinate system standard

### Loops
- Prefer foreach-style loops over traditional for loops when possible:
    ```java
    // Preferred
    for (Ship ship : ships) {
            ship.update();
    }
    
    // Avoid when not necessary
    for (int i = 0; i < ships.size(); i++) {
            Ship ship = ships.get(i);
            ship.update();
    }
    ```
- Use traditional for loops only when the index itself is needed

### Singleton Pattern
- Game modes follow the singleton pattern with a protected constructor and static getInstance() method:
  ```java
  private static GameMode instance = null;
  
  public static GameMode getInstance() {
      if (instance == null) {
          instance = new GameMode();
      }
      return instance;
  }
  ```

### Enum Usage
- Always use enums instead of strings or integers for fixed options
- Enums are used for fixed sets of options like `Direction`, `ShipType`, `RotationDirection`, etc.
- Enum methods are supported
- Always provide a default case in switch statements when using enums

### Method Naming
- Getters and setters follow Java bean conventions: `getX()`, `setX()`
- Method names use camelCase starting with lowercase letters
- Clear, descriptive names for methods describing their action

### Logging
- Always use `Game.LOGGER` for logging, never create new logger instances
- Use string concatenation with `+` for log messages, not object notation:
  ```java
  // Good
  Game.LOGGER.info("Player " + playerName + " fired at " + x + "," + y);
  
  // Bad
  Game.LOGGER.info(new Object[]{"Player", playerName, "fired at", x, y});
  ```

### Switch Statements
- Prefer enhanced switch expressions over traditional switch statements when possible:
  ```java
  // Preferred (expression-based switch)
  String message = switch(direction) {
      case UP -> "Moving up";
      case DOWN -> "Moving down";
      case LEFT -> "Moving left";
      case RIGHT -> "Moving right";
      default -> "Not moving";
  };
  
  // Avoid when not necessary (traditional switch)
  String message;
  switch(direction) {
      case UP:
          message = "Moving up";
          break;
      case DOWN:
          message = "Moving down";
          break;
      // etc.
  }
  ```
- Always include a default case in switch statements/expressions
- Prefer switch expressions over large if else blocks for cleaner code

## Class Hierarchy and Design Patterns

### Geometric Classes Explained
- **Position**: Represents a 2D position with `x` and `y` coordinates
  - Use when you only need position information
  - No width or height information

- **Box**: A size-only container with `sx` and `sy` (width and height)
  - "Locationless" - only describes dimensions with no position
  - Use for describing dimensions of objects without location

- **Rect**: Extends Box to include position information (`x` and `y`)
  - Combines position (Position) and dimensions (Box) into one object
  - Used for game objects that have both position and size
  - Use .asPosition() to represent the Position of a Rect (we can only inherit from one class)

### Game Component Relationships

- **Tile and TileData**: Separation of data and position
  - **TileData**: Pure data container storing game state information:
    - `isShot`: Whether the tile has been fired upon
    - `containedShip`: UUID of the ship occupying this tile (if any)
  - **Tile**: Connects a TileData with its position on the grid:
    - Wraps TileData and adds a coordinate
    - Represents the visual cell on the grid

- **Ship and ShipBox**: Similar separation of data and visualization
  - **Ship**: Core game object with position, type, status
  - **ShipBox**: Handles the visualization and spatial representation

### Game Architecture Components

- **GameMode**: Abstract singleton class defining game state phases
  - Each game mode handles its own rendering, input, and state changes
  - GameModes include: StartMenuMode, BuildMode (for placing ships)
  - Transition between modes through GameState's setMode method

- **GameState**: Global persistent state containing data that persists between modes
  - Holds the current mode and any global data (grids, players, etc.)

- **AsciiDisplay**: Rendering engine using ASCII characters in a JTextArea
  - Has a buffer represented as a char array
  - Refreshes the display through Swing UI updates

## Coordinate System
The game uses a 2D grid coordinate system with the following standards:
- Origin (0,0) is at the top-left corner
- X increases to the right
- Y increases downward
- Grid coordinates are integer values
- Direct x/y access is preferred over array index notation
- Display coordinates may be adjusted (e.g., `x * 2 + ox` for wider characters)

## Core Game Components

### Ship System
- Ships are defined by ShipType (BATTLESHIP4X1, CRUISER3X1, etc.)
- Each ship has:
  - Position (x, y)
  - Direction (UP, DOWN, LEFT, RIGHT)
  - A unique UUID
  - Ship status (isSunk)
- Ship appearance is determined by ShipBox, which contains:
  - Boolean matrix representing occupied cells
  - String array for visual representation
  - Direction and rotation support

### Grid System
- 10x10 grid of TileData objects
- Each grid is named for debugging (e.g., "Player")
- Contains a map of ships indexed by their UUID
- Provides methods for updating tiles and managing ships

## Game Architecture

### Game Mode System
Each GameMode implements four essential methods:
1. `enter()`: Called when entering the mode
2. `render(GameState, AsciiDisplay)`: Renders the current state
3. `update(GameState, InputManager)`: Processes input and updates state
4. `exit()`: Called when leaving the mode

The game transitions between modes as follows:
- StartMenuMode: Main menu and options
- BuildMode: Ship placement phase

### Input Handling
- InputManager processes raw keyboard and mouse input
- Events are queued and processed in the game loop
- Key events use Java's KeyEvent system
- Mouse position is converted to grid coordinates

## Game Loop
The game loop is implemented in the Game class:
   1. Render current mode using the display
   2. Update game state based on input and logic

The game loop follows this pattern:
```
while (isRunning) {
    state.getMode().render(state, display);
    state = state.getMode().update(state, inputManager);
}
```

This design allows each mode to control how it uses input and updates the game state, while maintaining a consistent loop structure.