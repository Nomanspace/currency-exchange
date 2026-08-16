package org.nomanspace.currencyexchange.exception;


public class InvalidDataException extends ApiException {
    public InvalidDataException(String message) {
        super(message, BAD_REQUEST);
    }
}
