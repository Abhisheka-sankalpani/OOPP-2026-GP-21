package exception;

/**
 * Thrown when a medical submission is rejected because the session
 * was not absent or the submission is for a session that does not exist.
 *
 * Demonstrates Exception Handling (OOP requirement for Member 2).
 */
public class MedicalSubmissionException extends Exception {

    public MedicalSubmissionException(String message) {
        super(message);
    }

    public MedicalSubmissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
