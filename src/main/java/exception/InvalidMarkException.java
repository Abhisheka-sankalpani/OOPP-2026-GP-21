package exception;

/** Thrown when a mark is outside the 0-100 range. */
public class InvalidMarkException extends Exception {
    public InvalidMarkException(String message) {
        super(message);
    }
}
