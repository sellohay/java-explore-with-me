package ru.yandex.practicum.emwservice.exception;

public class UpdateRequestException extends RuntimeException {
    public UpdateRequestException(String message) {
        super(message);
    }
}
