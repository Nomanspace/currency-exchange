package org.nomanspace.currencyexchange.exception;



public class MethodNotAllowedException extends ApiException {

    public MethodNotAllowedException() {
        super("Method Not Allowed", METHOD_NOT_ALLOWED);
    }
    public MethodNotAllowedException(String message) {
        super(message, METHOD_NOT_ALLOWED);
    }
}
