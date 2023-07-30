package ru.practicum.statisticservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleObjectValidationException(final ObjectValidationException e) {
        log.warn(e.getMessage());
        return new ApiError(
                e.getMessage(),
                String.format("Ошибка валидации, %s", e.getClass()),
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable e) {
        log.warn(e.getMessage());
        return new ApiError(
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
                e.getMessage(),
                "Ошибка валидации",
                HttpStatus.BAD_REQUEST,
                LocalDateTime.now()
        );
    }
}