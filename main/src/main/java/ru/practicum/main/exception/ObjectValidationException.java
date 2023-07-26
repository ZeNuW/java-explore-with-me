package ru.practicum.main.exception;

public class ObjectValidationException extends RuntimeException {

    public ObjectValidationException(String message) {
        super(message);
    }
}