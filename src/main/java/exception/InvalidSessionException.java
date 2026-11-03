package exception;

/**
 * Thrown when an invalid session number is supplied.
 * Session numbers must be between 1 and 15 (project rule).
 *
 * Demonstrates Exception Handling (OOP requirement for Member 2).
 */
public class InvalidSessionException extends Exception {

    private final int invalidSession;

    public InvalidSessionException(int session) {
        super("Invalid session number: " + session +
              ". Session must be between 1 and 15.");
        this.invalidSession = session;
    }

    public InvalidSessionException(String message) {
        super(message);
        this.invalidSession = -1;
    }

    public int getInvalidSession() {
        return invalidSession;
    }
}
