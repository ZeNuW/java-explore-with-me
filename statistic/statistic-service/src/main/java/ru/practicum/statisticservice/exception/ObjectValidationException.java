package ru.practicum.statisticservice.exception;

public class ObjectValidationException extends RuntimeException {

    public ObjectValidationException(String message) {
        super(message);
    }
}