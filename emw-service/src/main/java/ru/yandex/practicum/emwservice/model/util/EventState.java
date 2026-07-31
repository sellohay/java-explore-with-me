package ru.yandex.practicum.emwservice.model.util;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    public static EventState fromString(String state) {
        try {
            return EventState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown event state: " + state);
        }
    }
}
