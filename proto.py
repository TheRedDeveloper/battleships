import copy
from typing import List, Dict, Set, Tuple
from dataclasses import dataclass

# Define a ship with its cells and type


@dataclass
class Ship:
    cells: Set[Tuple[int, int]]  # Set of (row, col) coordinates
    type: str  # "U" or "2x1"
    hits: Set[Tuple[int, int]]  # Cells already hit


# Define a belief state as a list of ships
BeliefState = List[Ship]

# Define the grid and known information
GRID_ROWS, GRID_COLS = 3, 4
VARIABLE_CELLS = [(0, 0), (2, 0), (0, 3), (1, 3), (2, 2)]  # A, B, C, D, E
KNOWN_HITS = [(0, 1), (0, 2), (1, 1), (2, 1)]  # X cells
KNOWN_MISSES = [(1, 0), (1, 2), (2, 3)]  # 0 cells

# Define the three belief states
BELIEF_STATES = [
    # Belief 1: U at [(0,1),(0,2),(0,3),(1,1),(1,3)], 2x1 at [(2,1),(2,2)]
    [
        Ship(
            cells={(0, 1), (0, 2), (0, 3), (1, 1), (1, 3)},
            type="U",
            hits={(0, 1), (0, 2), (1, 1)}
        ),
        Ship(
            cells={(2, 1), (2, 2)},
            type="2x1",
            hits={(2, 1)}
        )
    ],
    # Belief 2: U at [(0,1),(0,2),(0,3),(1,1),(1,3)], 2x1 at [(2,0),(2,1)]
    [
        Ship(
            cells={(0, 1), (0, 2), (0, 3), (1, 1), (1, 3)},
            type="U",
            hits={(0, 1), (0, 2), (1, 1)}
        ),
        Ship(
            cells={(2, 0), (2, 1)},
            type="2x1",
            hits={(2, 1)}
        )
    ],
    # Belief 3: U at [(0,0),(0,1),(1,1),(2,0),(2,1)], 2x1 at [(0,2),(0,3)]
    [
        Ship(
            cells={(0, 0), (0, 1), (1, 1), (2, 0), (2, 1)},
            type="U",
            hits={(0, 1), (1, 1), (2, 1)}
        ),
        Ship(
            cells={(0, 2), (0, 3)},
            type="2x1",
            hits={(0, 2)}
        )
    ]
]


def is_game_won(belief: BeliefState) -> bool:
    """Check if all ships in the belief state are sunk."""
    return all(len(ship.cells) == len(ship.hits) for ship in belief)


def update_belief(belief: BeliefState, cell: Tuple[int, int], outcome: str) -> BeliefState:
    """Update a belief state after an action (miss, hit, or sunk)."""
    new_belief = []
    for ship in belief:
        new_ship = copy.deepcopy(ship)
        if cell in ship.cells:
            if outcome == "hit":
                new_ship.hits.add(cell)
            elif outcome == "sunk":
                new_ship.hits = new_ship.cells.copy()
        new_belief.append(new_ship)
    return new_belief


def filter_beliefs(beliefs: List[BeliefState], cell: Tuple[int, int], outcome: str, debug: bool = False) -> List[BeliefState]:
    """Filter belief states based on the outcome of hitting a cell."""
    filtered = []
    for i, belief in enumerate(beliefs):
        if outcome == "miss":
            if not any(cell in ship.cells for ship in belief):
                filtered.append(belief)
        elif outcome == "hit":
            for ship in belief:
                if cell in ship.cells and len(ship.hits) + 1 < len(ship.cells):
                    filtered.append(update_belief(belief, cell, "hit"))
                    break
        elif outcome == "sunk":
            for ship in belief:
                if cell in ship.cells and len(ship.hits) + 1 == len(ship.cells):
                    filtered.append(update_belief(belief, cell, "sunk"))
                    break
    return filtered


def calculate_moves_to_win(variable_cells: List[Tuple[int, int]], beliefs: List[BeliefState], depth: int = 0, memo: Dict = None) -> float:
    """Recursively calculate the expected number of moves to win."""
    if memo is None:
        memo = {}

    belief_key = tuple(tuple(tuple(sorted(ship.hits)) + (ship.type,) for ship in belief) for belief in sorted(
        beliefs, key=lambda b: tuple(sorted((ship.type, tuple(sorted(ship.hits))) for ship in b))
    ))
    cells_key = tuple(sorted(variable_cells))
    key = (cells_key, belief_key)

    if key in memo:
        return memo[key]

    if all(is_game_won(belief) for belief in beliefs):
        return 0

    for cell in variable_cells:
        is_hit = all(
            any(
                cell in ship.cells and len(ship.hits) + 1 <= len(ship.cells) for ship in belief
            ) for belief in beliefs)
        is_sink = all(any(cell in ship.cells and len(ship.hits) + 1 == len(ship.cells) for ship in belief) for belief in beliefs)
        if is_hit and not is_sink:
            new_cells = [c for c in variable_cells if c != cell]
            new_beliefs = filter_beliefs(beliefs, cell, "hit", debug=True)
            result = 1 + calculate_moves_to_win(new_cells, new_beliefs, depth + 1, memo)
            memo[key] = result
            return result
        elif is_sink:
            new_cells = [c for c in variable_cells if c != cell]
            new_beliefs = filter_beliefs(beliefs, cell, "sunk", debug=True)
            result = 1 + calculate_moves_to_win(new_cells, new_beliefs, depth + 1, memo)
            memo[key] = result
            return result

    min_moves = float('inf')
    for cell in variable_cells:
        total_moves = 0
        total_weight = 0

        miss_beliefs = filter_beliefs(beliefs, cell, "miss", debug=True)
        if miss_beliefs:
            weight = len(miss_beliefs)
            moves = 1 + calculate_moves_to_win([c for c in variable_cells if c != cell], miss_beliefs, depth + 1, memo)
            total_moves += weight * moves
            total_weight += weight

        hit_beliefs = filter_beliefs(beliefs, cell, "hit", debug=True)
        if hit_beliefs:
            weight = len(hit_beliefs)
            moves = 1 + calculate_moves_to_win([c for c in variable_cells if c != cell], hit_beliefs, depth + 1, memo)
            total_moves += weight * moves
            total_weight += weight

        sunk_beliefs = filter_beliefs(beliefs, cell, "sunk", debug=True)
        if sunk_beliefs:
            weight = len(sunk_beliefs)
            moves = 1 + calculate_moves_to_win([c for c in variable_cells if c != cell], sunk_beliefs, depth + 1, memo)
            total_moves += weight * moves
            total_weight += weight

        if total_weight > 0:
            expected_moves = total_moves / total_weight
            min_moves = min(min_moves, expected_moves)

    memo[key] = min_moves
    return min_moves


def optimal_move(variable_cells: List[Tuple[int, int]], beliefs: List[BeliefState]) -> Tuple[List[Tuple[int, int]], float]:
    """Find the cells with the lowest expected moves to win."""
    # Check for cells that guarantee a hit or sink for all belief states
    for cell in variable_cells:
        is_hit = all(any(cell in ship.cells and len(ship.hits) + 1 <= len(ship.cells) for ship in belief) for belief in beliefs)
        is_sink = all(any(cell in ship.cells and len(ship.hits) + 1 == len(ship.cells) for ship in belief) for belief in beliefs)
        if is_hit and not is_sink:
            new_cells = [c for c in variable_cells if c != cell]
            new_beliefs = filter_beliefs(beliefs, cell, "hit", debug=True)
            moves = 1 + calculate_moves_to_win(new_cells, new_beliefs)
            return [cell], moves
        elif is_sink:
            new_cells = [c for c in variable_cells if c != cell]
            new_beliefs = filter_beliefs(beliefs, cell, "sunk", debug=True)
            moves = 1 + calculate_moves_to_win(new_cells, new_beliefs)
            return [cell], moves

    # Calculate expected moves for each cell
    best_cells = []
    best_moves = float('inf')
    expected_moves_by_cell = {}

    # Calculate the expected moves for each cell
    for cell in variable_cells:
        total_moves = 0
        total_weight = 0

        miss_beliefs = filter_beliefs(beliefs, cell, "miss", debug=True)
        if miss_beliefs:
            weight = len(miss_beliefs)
            moves = 1 + calculate_moves_to_win([c for c in variable_cells if c != cell], miss_beliefs)
            total_moves += weight * moves
            total_weight += weight

        hit_beliefs = filter_beliefs(beliefs, cell, "hit", debug=True)
        if hit_beliefs:
            weight = len(hit_beliefs)
            moves = 1 + calculate_moves_to_win([c for c in variable_cells if c != cell], hit_beliefs)
            total_moves += weight * moves
            total_weight += weight

        sunk_beliefs = filter_beliefs(beliefs, cell, "sunk", debug=True)
        if sunk_beliefs:
            weight = len(sunk_beliefs)
            moves = 1 + calculate_moves_to_win([c for c in variable_cells if c != cell], sunk_beliefs)
            total_moves += weight * moves
            total_weight += weight

        if total_weight > 0:
            # Calculate expected moves as the weighted average
            expected_moves = total_moves / total_weight
            expected_moves_by_cell[cell] = expected_moves

            # Update best cells if this cell has fewer expected moves
            if expected_moves < best_moves:
                best_moves = expected_moves
                best_cells = [cell]
            # Add to the list of best cells if it's equal to the current best
            elif expected_moves == best_moves:
                best_cells.append(cell)

    return best_cells, best_moves


def main():
    cell_names = {(0, 0): 'A', (2, 0): 'B', (0, 3): 'C', (1, 3): 'D', (2, 2): 'E'}

    # Find the optimal cells
    best_cells, expected_moves = optimal_move(VARIABLE_CELLS, BELIEF_STATES)

    # Format the output for multiple optimal cells
    optimal_cells_str = ", ".join([cell_names[cell] for cell in best_cells])

    # Calculate the expected moves for each cell to print detailed information
    expected_moves_per_cell = {}

    for cell in VARIABLE_CELLS:
        miss_beliefs = filter_beliefs(BELIEF_STATES, cell, "miss", debug=True)
        hit_beliefs = filter_beliefs(BELIEF_STATES, cell, "hit", debug=True)
        sunk_beliefs = filter_beliefs(BELIEF_STATES, cell, "sunk", debug=True)
        total_moves = 0
        total_weight = 0

        if miss_beliefs:
            weight = len(miss_beliefs)
            moves = 1 + calculate_moves_to_win([c for c in VARIABLE_CELLS if c != cell], miss_beliefs)
            total_moves += weight * moves
            total_weight += weight
            print(f"{cell_names[cell]} miss: {weight/3*100:.0f}%")

        if hit_beliefs:
            weight = len(hit_beliefs)
            moves = 1 + calculate_moves_to_win([c for c in VARIABLE_CELLS if c != cell], hit_beliefs)
            total_moves += weight * moves
            total_weight += weight
            print(f"{cell_names[cell]} hit: {weight/3*100:.0f}%")

        if sunk_beliefs:
            weight = len(sunk_beliefs)
            moves = 1 + calculate_moves_to_win([c for c in VARIABLE_CELLS if c != cell], sunk_beliefs)
            total_moves += weight * moves
            total_weight += weight
            print(f"{cell_names[cell]} sink: {weight/3*100:.0f}%")

        expected_moves_per_cell[cell] = total_moves / total_weight
        print(f"a({cell_names[cell]}) = {expected_moves_per_cell[cell]:.2f}\n")

    # Use the expected moves directly from the per-cell calculation to ensure consistency
    if best_cells:
        example_best_cell = best_cells[0]
        expected_moves = expected_moves_per_cell[example_best_cell]
        print(f"Optimal move(s): {optimal_cells_str} with expected moves: {expected_moves:.2f}")


if __name__ == "__main__":
    main()
