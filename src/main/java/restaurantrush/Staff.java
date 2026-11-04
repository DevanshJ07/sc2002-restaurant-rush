package restaurantrush;

/** Shared state of all staff: a name and a "has acted this turn" flag (one task per turn). */
public abstract class Staff {
    private final String name;
    private boolean hasActedThisTurn;

    protected Staff(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Staff name must not be empty");
        }
        this.name = name;
        this.hasActedThisTurn = false;
    }

    public String getName() {
        return name;
    }

    public boolean hasActedThisTurn() {
        return hasActedThisTurn;
    }

    public void resetForNewTurn() {
        hasActedThisTurn = false;
    }

    /** Called by subclasses only after an action fully succeeded. */
    protected void markActed() {
        hasActedThisTurn = true;
    }

    /** Subclass helper: reject a second task in the same turn. */
    protected void requireNotActed() throws InvalidActionException {
        if (hasActedThisTurn) {
            throw new InvalidActionException(name + " has already acted this turn.");
        }
    }
}
