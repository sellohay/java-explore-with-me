package ru.yandex.practicum.emwservice.model.util.projections;

public interface ConfirmedRequestsCount {
    Long getEventId();

    Integer getCount();
}