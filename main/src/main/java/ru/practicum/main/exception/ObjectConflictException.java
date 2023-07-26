package ru.practicum.main.exception;

public class ObjectConflictException extends RuntimeException {
    public ObjectConflictException(String message) {
        super(message);
    }
}