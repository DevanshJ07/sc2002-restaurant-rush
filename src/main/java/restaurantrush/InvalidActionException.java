package restaurantrush;

/**
 * Thrown when the player asks for an action that is not allowed right now.
 * The message is the reason shown to the player; GameCLI catches it and asks again.
 */
public class InvalidActionException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidActionException(String message) {
        super(message);
    }
}
