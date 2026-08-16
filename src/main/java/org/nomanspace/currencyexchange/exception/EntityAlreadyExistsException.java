package org.nomanspace.currencyexchange.exception;



public class EntityAlreadyExistsException extends ApiException {
    public EntityAlreadyExistsException(String message)
    {
        super(message, CONFLICT);
    }
}
