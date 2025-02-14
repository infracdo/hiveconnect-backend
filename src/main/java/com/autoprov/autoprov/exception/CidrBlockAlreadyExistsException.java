package com.autoprov.autoprov.exception;


public class CidrBlockAlreadyExistsException extends RuntimeException {
    public CidrBlockAlreadyExistsException(String message) {
        super(message);
    }
}