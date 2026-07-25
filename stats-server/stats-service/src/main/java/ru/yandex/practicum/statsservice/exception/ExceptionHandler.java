package ru.yandex.practicum.statsservice.exception;

import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.statsdto.dtos.ErrorResponse;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler({ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationError(final ValidationException exception) {
        return new ErrorResponse("Ошибка валидации: ", exception.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleArgumentError(final IllegalArgumentException exception) {
        return new ErrorResponse("Ошибка аргументов: ", exception.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotValidArgumentError(final MethodArgumentNotValidException exception) {
        return new ErrorResponse("Ошибка аргументов: ", exception.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({DateTimeParseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleNotValidArgumentError(final DateTimeParseException exception) {
        return new ErrorResponse("Некорректный формат даты: ", exception.getMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalError(final RuntimeException exception) {
        return new ErrorResponse("Неизвестная ошибка: ", exception.getMessage());
    }
}
