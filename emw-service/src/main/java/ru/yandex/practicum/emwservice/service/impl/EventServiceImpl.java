package ru.yandex.practicum.emwservice.service.impl;

import jakarta.validation.ValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.event.*;
import ru.yandex.practicum.emwservice.dtos.mappers.EventMapper;
import ru.yandex.practicum.emwservice.exception.DateCreationException;
import ru.yandex.practicum.emwservice.exception.NotFoundException;
import ru.yandex.practicum.emwservice.exception.UpdateEventException;
import ru.yandex.practicum.emwservice.exception.UpdateRequestException;
import ru.yandex.practicum.emwservice.model.Category;
import ru.yandex.practicum.emwservice.model.Event;
import ru.yandex.practicum.emwservice.model.User;
import ru.yandex.practicum.emwservice.model.util.*;
import ru.yandex.practicum.emwservice.repository.EventRepository;
import ru.yandex.practicum.emwservice.repository.RequestRepository;
import ru.yandex.practicum.emwservice.service.interfaces.CategoryService;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;
import ru.yandex.practicum.statsclient.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    public EventServiceImpl(EventRepository eventRepository, UserService userService,
                            CategoryService categoryService, RequestRepository requestRepository, StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.requestRepository = requestRepository;
        this.statsClient = statsClient;
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.plusHours(2).isAfter(newEventDto.getEventDate())) {
            String errorMessage = String.format("Field: eventDate. Error: must be at least " +
                            "2 hours after now. Value: %s", newEventDto.getEventDate());
            throw new DateCreationException(errorMessage);
        }

        Event event = EventMapper.newToEvent(newEventDto);
        Category category = categoryService.getCategoryEntity(newEventDto.getCategoryId());
        User initiator = userService.getUserEntity(userId);
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setCreatedOn(now);

        event = eventRepository.save(event);
        EventFullDto dto = EventMapper.eventToEventFullDto(event);
        dto.setConfirmedRequests(0);
        dto.setViews(0L);
        return dto;
    }

    @Override
    public Event getEventEntity(Long id) {
        return eventRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Event with id " + id + " does not exist"));
    }

    @Override
    public EventFullDto getEventById(Long id) {
        Event event = getEventEntity(id);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new UpdateEventException("Event with id " + id + " has not been published");
        }
        return mapToEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEvents(String text, List<Integer> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                         EventSortOption sortOption, int from, int size) {
        List<Event> events;
        if (sortOption == EventSortOption.EVENT_DATE) {
            events = eventRepository.findPublishedWithFiltersSortEventDate(text, categories,
                    paid, rangeStart, rangeEnd, onlyAvailable, from, size);
        } else {
            events = eventRepository.findPublishedWithFiltersSortViews(text, categories, paid,
                    rangeStart, rangeEnd, onlyAvailable);
        }

        List<EventShortDto> dtos = mapToEventShortDtoList(events);

        if (sortOption == EventSortOption.VIEWS) {
            return dtos.stream()
                    .sorted((dto1, dto2) -> Long.compare(dto2.getViews(), dto1.getViews()))
                    .skip(from)
                    .limit(size)
                    .toList();
        }

        return dtos;

    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        userService.isUserExist(userId);
        List<Event> events = eventRepository.getEventsByUser(userId, from, size);
        return mapToEventShortDtoList(events);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        userService.isUserExist(userId);
        Event event = getEventEntity(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User with id " + userId + " does not have an event with id=" + eventId);
        }
        return mapToEventFullDto(event);
    }

    @Override
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        userService.isUserExist(userId);
        Event event = getEventEntity(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User with id " + userId + " does not have an event with id " + eventId);
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new UpdateRequestException("Only pending or canceled events can be changed");
        }
        event = EventMapper.updateEventFieldsUser(event, request);
        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryEntity(request.getCategoryId());
            event.setCategory(category);
        }
        event = eventRepository.save(event);
        return mapToEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<String> states,
                                             List<Long> categories, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, int from, int size) {
        List<Event> events = eventRepository.findAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
        return mapToEventFullDtoList(events);
    }

    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventEntity(eventId);
        LocalDateTime now = LocalDateTime.now();
        if (request.getEventDate() != null && now.plusHours(1).isAfter(request.getEventDate())) {
            String errorMessage = String.format("Field: eventDate. Error: must be at least " +
                    "1 hour after now. Value: %s", request.getEventDate());
            throw new DateCreationException(errorMessage);
        }
        StateActionAdmin action = request.getStateAction();
        if (action.equals(StateActionAdmin.PUBLISH_EVENT) && !event.getState().equals(EventState.PENDING)) {
            throw new UpdateEventException("Only pending event can be published");
        }
        if (action.equals(StateActionAdmin.REJECT_EVENT) && event.getState().equals(EventState.PUBLISHED)) {
            throw new UpdateEventException("Only not published event can be published");
        }
        event = EventMapper.updateEventFieldsAdmin(event, request);
        event = eventRepository.save(event);
        return mapToEventFullDto(event);
    }

    @Override
    public List<EventShortDto> mapToEventShortDtoList(List<Event> events) {
        Map<Long, Long> viewsMap = getViewsMap(events);
        Map<Long, Integer> confirmedRequestsMap = getConfReqMap(events);

        return events.stream()
                .map(event -> {
                    int confReq = confirmedRequestsMap.getOrDefault(event.getId(), 0);
                    long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return mapToEventShortDto(event, confReq, views);
                })
                .toList();

    }

    @Override
    public List<Event> getByIds(List<Long> ids) {
        return eventRepository.findAllByIdIn(ids);
    }

    private EventFullDto mapToEventFullDto(Event event) {
        Map<Long, Long> viewsMap = getViewsMap(List.of(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);
        int confReq = requestRepository.countByEventIdAndStatus(event.getId(), RequestState.CONFIRMED);

        EventFullDto dto = EventMapper.eventToEventFullDto(event);
        dto.setConfirmedRequests(confReq);
        dto.setViews(views);
        return dto;
    }

    private EventFullDto mapToEventFullDto(Event event, int confReq, Long views) {
        EventFullDto dto = EventMapper.eventToEventFullDto(event);
        dto.setConfirmedRequests(confReq);
        dto.setViews(views);
        return dto;
    }

    private EventShortDto mapToEventShortDto(Event event, int confReq, long views) {
        EventShortDto dto = EventMapper.eventToEventShortDto(event);
        dto.setConfirmedRequests(confReq);
        dto.setViews(views);
        return dto;
    }

    private List<EventFullDto> mapToEventFullDtoList(List<Event> events) {
        Map<Long, Long> viewsMap = getViewsMap(events);
        Map<Long, Integer> confirmedRequestsMap = getConfReqMap(events);

        return events.stream()
                .map(event -> {
                    int confReq = confirmedRequestsMap.getOrDefault(event.getId(), 0);
                    long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return mapToEventFullDto(event, confReq, views);
                })
                .toList();
    }

    private Map<Long, Long> getViewsMap(List<Event> events) {
        if (events.isEmpty()) {
            return new HashMap<>();
        }

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(10));
        LocalDateTime end = LocalDateTime.now();

        String startStr = start.format(formatter);
        String endStr = end.format(formatter);
        ResponseEntity<Object> response = statsClient.getStats(startStr, endStr, uris, true);
        Object body = response.getBody();

        Map<Long, Long> viewsMap = new HashMap<>();
        if (body instanceof List) {
            List<Map<String, Object>> statsList = (List<Map<String, Object>>) body;

            for (Map<String, Object> stat : statsList) {
                String uri = (String) stat.get("uri");
                Long hits = stat.get("hits") != null ? (Long) stat.get("hits") : 0L;

                if (uri != null && uri.startsWith("/events/")) {
                    Long eventId = Long.parseLong(uri.replace("/events/", ""));
                    viewsMap.put(eventId, hits);
                }
            }
        }

        return viewsMap;
    }

    private Map<Long, Integer> getConfReqMap(List<Event> events) {
        if (events.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        return requestRepository.getConfReqCounts(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        ConfirmedRequestsCount::getEventId,
                        ConfirmedRequestsCount::getCount
                ));
    }
}
