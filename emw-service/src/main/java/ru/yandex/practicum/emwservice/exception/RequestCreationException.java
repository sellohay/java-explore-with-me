package ru.yandex.practicum.emwservice.exception;

public class RequestCreationException extends RuntimeException {
    public RequestCreationException(String message) {
        super(message);
    }
}
