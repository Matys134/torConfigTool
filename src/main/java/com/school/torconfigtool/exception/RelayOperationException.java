package com.school.torconfigtool.exception;

/**
 * This is a class named RelayOperationException.
 * It extends RuntimeException, which is a form of Throwable that indicates conditions that a reasonable application might want to catch.
 *
 * The RelayOperationException class has a constructor that accepts a message of type String.
 * This message is then passed to the superclass constructor of RuntimeException.
 */
public class RelayOperationException extends RuntimeException {

    /**
     * This is the constructor for the RelayOperationException class.
     * It accepts a message parameter of type String.
     *
     * @param message A detailed message about the exception. The detail message is saved for later retrieval by the Throwable.getMessage() method.
     */
    public RelayOperationException(String message) {
        super(message);
    }
}