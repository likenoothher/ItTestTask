package com.azierets.restapijwt.exceptionhandler.exception;

public class UserIsAlreadyRegisteredException extends RuntimeException {
    public UserIsAlreadyRegisteredException(String message) {
        super(message);
    }
}
