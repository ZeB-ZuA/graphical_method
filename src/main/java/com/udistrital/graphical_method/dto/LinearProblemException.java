package com.udistrital.graphical_method.dto;

public class LinearProblemException extends RuntimeException {

     public LinearProblemException(String message) {
        super(message);
    }

    public LinearProblemException(String message, Throwable cause) {
        super(message, cause);
    }
}
