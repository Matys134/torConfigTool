package com.school.torconfigtool.exception;

/**
 * This class represents a custom exception that is thrown when an operation on a relay fails.
 * It extends the RuntimeException class, meaning it's an unchecked exception.
 */
public class RelayOperationException extends RuntimeException {

    /**
     * Constructs a new RelayOperationException with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
     */
    public RelayOperationException(String message) {
        super(message);
    }
}