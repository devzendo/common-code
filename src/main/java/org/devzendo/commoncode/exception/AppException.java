package org.devzendo.commoncode.exception;

/**
 * An application-level exception
 * @author matt
 *
 */
@SuppressWarnings("serial")
public final class AppException extends Exception {
    /**
     * An application exception with no inforamtion or cause 
     */
    public AppException() {
        super();
    }

    /**
     * An application exception with a message  
     * @param message the reason why the exception is being raised
     */
    public AppException(final String message) {
        super(message);
    }

    /**
     * An application exception with a message and a cause
     * @param message the reason why the exception is being raised
     * @param cause why? what's the underlying reason?
     */
    public AppException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * An application exception with a cause
     * @param cause why? what's the underlying reason?
     */
    public AppException(final Throwable cause) {
        super(cause);
    }
}
