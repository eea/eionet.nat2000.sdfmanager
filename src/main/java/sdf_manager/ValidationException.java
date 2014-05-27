package sdf_manager;

/**
 * Base exception for validation errors.
 *
 * @author Jaanus
 */
public class ValidationException extends Exception {

    /**  */
    private static final long serialVersionUID = 1109436615235859620L;

    /**
     * Class constructor.
     */
    public ValidationException() {
        super();
    }

    /**
     * Class constructor.
     * @param message
     * @param cause
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Class constructor.
     * @param message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Class constructor.
     * @param cause
     */
    public ValidationException(Throwable cause) {
        super(cause);
    }
}
