package com.cloud.demo.exceptions;

public class JwtValidationException extends RuntimeException {
    public JwtValidationException() {
        super();
    }

    public JwtValidationException(String message) {
        super(message);
    }

    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
