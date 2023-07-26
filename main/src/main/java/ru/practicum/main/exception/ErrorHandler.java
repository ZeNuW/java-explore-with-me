package ru.practicum.main.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Arrays;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidError(final RuntimeException e) {
        log.warn(e.getMessage());
        return new ApiError(
                Arrays.toString(e.getStackTrace()),
                e.getMessage(),
                String.format("Ошибка валидации, %s", e.getClass()),
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleObjectValidationException(final ObjectValidationException e) {
        log.warn(e.getMessage());
        return new ApiError(
                Arrays.toString(e.getStackTrace()),
                e.getMessage(),
                String.format("Ошибка валидации, %s", e.getClass()),
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleObjectNotExistException(final ObjectNotExistException e) {
        log.warn(e.getMessage());
        return new ApiError(
                Arrays.toString(e.getStackTrace()),
                e.getMessage(),
                String.format("Объект не был найден, %s", e.getClass()),
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.warn(e.getMessage());
        return new ApiError(
                Arrays.toString(e.getStackTrace()),
                e.getMessage(),
                String.format("Необрабатываемая ошибка, %s", e.getClass()),
                HttpStatus.INTERNAL_SERVER_ERROR,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            MissingRequestHeaderException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentTypeMismatchExceptionD(Exception e) {
        log.warn("Ошибка {}, описание: {}", e.getClass(), e.getMessage());
        return new ApiError(
                Arrays.toString(e.getStackTrace()),
                e.getMessage(),
                "Ошибка валидации",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleHttpMessageNotReadableException(final HttpMessageNotReadableException e) {
        log.warn("Ошибка HttpMessageNotReadableException {}", e.getMessage());
        return new ApiError(
                Arrays.toString(e.getStackTrace()),
                e.getMessage(),
                "Ошибка в формате JSON",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleSqlException(final DataIntegrityViolationException e) {
        log.warn("Ошибка DataIntegrityViolationException {}", e.getMessage());
        return new ApiError(
                Arrays.toString(e.getStackTrace()),
                e.getMessage(),
                "Data integrity exception",
                HttpStatus.CONFLICT,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleObjectConflictException(final ObjectConflictException e) {
        log.warn(e.getMessage());
        return new ApiError(
                Arrays.toString(e.getStackTrace()),
                e.getMessage(),
                String.format("Конфликт в данных, %s", e.getClass()),
                HttpStatus.CONFLICT,
                LocalDateTime.now()
        );
    }
}