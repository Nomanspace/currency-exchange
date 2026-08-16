package org.nomanspace.currencyexchange.exception;


public class DatabaseException extends ApiException {

    public DatabaseException(String message) {
        super(message,INTERNAL_SERVER_ERROR);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause, INTERNAL_SERVER_ERROR);
    }
}
