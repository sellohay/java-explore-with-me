package ru.yandex.practicum.emwservice.exception;

import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @org.springframework.web.bind.annotation.ExceptionHandler({
            ValidationException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationError(final Exception exception) {
        return new ApiError(
                null,
                exception.getMessage(),
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now().format(FORMATTER)
        );
    }


    @org.springframework.web.bind.annotation.ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNotValidArgumentError(final MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> "Field: " + error.getField() +
                        ". Error: " + error.getDefaultMessage() +
                        ". Value: " + error.getRejectedValue())
                .collect(Collectors.toList());

        String message = errors.isEmpty() ? exception.getMessage() : errors.getFirst();
        return new ApiError(
                errors,
                message,
                "Incorrectly made request.",
                HttpStatus.BAD_REQUEST.name(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationError(final DataIntegrityViolationException exception) {
        return new ApiError(
                null,
                exception.getMessage(),
                "Integrity constraint has been violated.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({
            CategoryNotEmptyException.class,
            RequestCreationException.class,
            UpdateRequestException.class,
            UpdateEventException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictError(final Exception exception) {
        return new ApiError(
                null,
                exception.getMessage(),
                "For the requested operation the conditions are not met.",
                HttpStatus.CONFLICT.name(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({
            DateCreationException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleForbiddenError(final Exception exception) {
        return new ApiError(
                null,
                exception.getMessage(),
                "For the requested operation the conditions are not met.",
                "FORBIDDEN",
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({
            NotFoundException.class,
            NoHandlerFoundException.class,
            NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundError(final Exception exception) {
        return new ApiError(
                null,
                exception.getMessage(),
                "The required object was not found.",
                HttpStatus.NOT_FOUND.name(),
                LocalDateTime.now().format(FORMATTER)
        );
    }

    @org.springframework.web.bind.annotation.ExceptionHandler({RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleInternalError(final RuntimeException exception) {
        return new ApiError(
                null,
                exception.getMessage(),
                "Internal server error.",
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                LocalDateTime.now().format(FORMATTER)
        );
    }
}
