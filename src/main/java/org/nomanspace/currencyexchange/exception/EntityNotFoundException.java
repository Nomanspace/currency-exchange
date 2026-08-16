package org.nomanspace.currencyexchange.exception;


public class EntityNotFoundException extends ApiException {
    public EntityNotFoundException(String message) {
        super(message, NOT_FOUND);
    }
}
