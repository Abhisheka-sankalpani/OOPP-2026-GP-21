package exception;

/** Wraps SQL errors so the GUI never has to deal with SQLException directly. */
public class DatabaseException extends Exception {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}
