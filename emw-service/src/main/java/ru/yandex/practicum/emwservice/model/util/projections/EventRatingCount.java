package ru.yandex.practicum.emwservice.model.util.projections;

public interface EventRatingCount {
    Long getEventId();

    Integer getLikes();

    Integer getDislikes();
}
