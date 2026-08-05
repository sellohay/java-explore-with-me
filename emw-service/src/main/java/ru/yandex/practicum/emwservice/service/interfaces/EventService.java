package ru.yandex.practicum.emwservice.service.interfaces;

import ru.yandex.practicum.emwservice.dtos.event.*;
import ru.yandex.practicum.emwservice.model.Event;
import ru.yandex.practicum.emwservice.model.util.EventSortOption;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    Event getEventEntity(Long id);

    EventFullDto getEventById(Long id);

    List<EventShortDto> getEvents(String text, List<Integer> categories, Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd, Boolean onlyAvailable,
                                  EventSortOption sortOption, int from, int size);

    List<EventShortDto> getEventsByUser(Long userId, int from, int size);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request);

    List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories,
                                      LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request);

    List<EventShortDto> mapToEventShortDtoList(List<Event> events);

    List<Event> getByIds(List<Long> ids);
}
