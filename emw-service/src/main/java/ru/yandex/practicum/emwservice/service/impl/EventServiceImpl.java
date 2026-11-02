package ru.yandex.practicum.emwservice.service.impl;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.event.*;
import ru.yandex.practicum.emwservice.dtos.mappers.EventMapper;
import ru.yandex.practicum.emwservice.exception.NotFoundException;
import ru.yandex.practicum.emwservice.exception.UpdateEventException;
import ru.yandex.practicum.emwservice.exception.UpdateRequestException;
import ru.yandex.practicum.emwservice.model.Category;
import ru.yandex.practicum.emwservice.model.Event;
import ru.yandex.practicum.emwservice.model.User;
import ru.yandex.practicum.emwservice.model.util.enums.EventSortOption;
import ru.yandex.practicum.emwservice.model.util.enums.EventState;
import ru.yandex.practicum.emwservice.model.util.enums.StateAction;
import ru.yandex.practicum.emwservice.model.util.enums.StateActionAdmin;
import ru.yandex.practicum.emwservice.repository.EventRepository;
import ru.yandex.practicum.emwservice.service.interfaces.CategoryService;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventUtilService eventUtilService;


    public EventServiceImpl(EventRepository eventRepository, UserService userService,
                            CategoryService categoryService, EventUtilService eventUtilService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
        this.categoryService = categoryService;
        this.eventUtilService = eventUtilService;
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        validateNewEvent(userId, newEventDto);

        Event event = EventMapper.newToEvent(newEventDto);
        Category category = categoryService.getCategoryEntity(newEventDto.getCategoryId());
        User initiator = userService.getUserEntity(userId);
        event.setCategory(category);
        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now());

        event = eventRepository.save(event);
        EventFullDto dto = EventMapper.eventToEventFullDto(event);
        dto.setConfirmedRequests(0);
        dto.setViews(0L);
        dto.setLikes(0);
        dto.setDislikes(0);
        dto.setRating(0);
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
            throw new NotFoundException("Event with id " + id + " has not been published");
        }
        return eventUtilService.mapToEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEvents(String text, List<Integer> categories, Boolean paid,
                                         LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                         EventSortOption sortOption, int from, int size) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("Start date must be before end date");
        }
        Boolean hasCategories = (categories != null && !categories.isEmpty());
        List<Integer> validCategories = hasCategories ? categories : null;

        if (sortOption.equals(EventSortOption.EVENT_DATE)) {
            List<Event> events = eventRepository.findPublishedWithFiltersSortEventDate(text, hasCategories, validCategories,
                    paid, rangeStart, rangeEnd, onlyAvailable, from, size);
            return eventUtilService.mapToEventShortDtoList(events);
        } else if (sortOption.equals(EventSortOption.RATING)) {
            List<Event> events = eventRepository.findPublishedWithFiltersSortRating(text, hasCategories, validCategories,
                    paid, rangeStart, rangeEnd, onlyAvailable, from, size);
            return eventUtilService.mapToEventShortDtoList(events);
        }

        List<Long> ids = eventRepository.findPublishedIdsWithFiltersSortViews(text, hasCategories, validCategories, paid,
                    rangeStart, rangeEnd, onlyAvailable);
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> uris = ids.stream()
                .map(id -> "/events/" + id)
                .toList();

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusYears(10);
        Map<Long, Long> eventsMap = eventUtilService.getStatsMap(start, end, uris);

        List<Long> eventsIds = ids.stream()
                .sorted(Comparator.comparingLong(id -> eventsMap.getOrDefault(id, 0L)))
                .skip(from)
                .limit(size)
                .toList();

        List<Event> limitedEvents = eventRepository.findAllByIdIn(eventsIds);
        List<EventShortDto> dtos = eventUtilService.mapToEventShortDtoList(limitedEvents);

        return dtos.stream()
                .sorted((dto1, dto2) -> Long.compare(dto2.getViews(), dto1.getViews()))
                .skip(from)
                .limit(size)
                .toList();
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, int from, int size) {
        userService.isUserExist(userId);
        List<Event> events = eventRepository.getEventsByUser(userId, from, size);
        return eventUtilService.mapToEventShortDtoList(events);
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        userService.isUserExist(userId);
        Event event = getEventEntity(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User with id " + userId + " does not have an event with id=" + eventId);
        }
        return eventUtilService.mapToEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        userService.getUserEntity(userId);
        Event event = validateUpdateEventUser(userId, eventId, request);
        event = EventMapper.updateEventFields(event, request);
        if (request.getStateAction() != null) {
            if (request.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            } else {
                event.setState(EventState.PENDING);
            }
        }
        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryEntity(request.getCategoryId());
            event.setCategory(category);
        }
        event = eventRepository.save(event);
        return eventUtilService.mapToEventFullDto(event);
    }

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<String> states,
                                             List<Long> categories, LocalDateTime rangeStart,
                                             LocalDateTime rangeEnd, int from, int size) {
        Boolean hasUsers = (users != null && !users.isEmpty());
        Boolean hasStates = (states != null && !states.isEmpty());
        Boolean hasCategories = (categories != null && !categories.isEmpty());

        List<Long> validUsers = hasUsers ? users : null;
        List<String> validStates = hasStates ? states : null;
        List<Long> validCategories = hasCategories ? categories : null;

        List<Event> events = eventRepository.findAdminEvents(
                hasUsers, validUsers,
                hasStates, validStates,
                hasCategories, validCategories,
                rangeStart, rangeEnd, from, size
        );
        return eventUtilService.mapToEventFullDtoList(events);
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = validateUpdateEventAdmin(eventId, request);
        event = EventMapper.updateEventFields(event, request);
        if (request.getStateAction() != null) {
            if (request.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else {
                event.setState(EventState.CANCELED);
            }
        }
        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryEntity(request.getCategoryId());
            event.setCategory(category);
        }
        event = eventRepository.save(event);
        return eventUtilService.mapToEventFullDto(event);
    }

    private Event validateUpdateEventAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventEntity(eventId);
        LocalDateTime now = LocalDateTime.now();
        if (request.getEventDate() != null && now.plusHours(1).isAfter(request.getEventDate())) {
            String errorMessage = String.format("Field: eventDate. Error: must be at least " +
                    "1 hour after now. Value: %s", request.getEventDate());
            throw new ValidationException(errorMessage);
        }
        StateActionAdmin action = request.getStateAction();
        if (action != null) {
            if (action.equals(StateActionAdmin.PUBLISH_EVENT) && !event.getState().equals(EventState.PENDING)) {
                throw new UpdateEventException("Only pending event can be published");
            }
            if (action.equals(StateActionAdmin.REJECT_EVENT) && event.getState().equals(EventState.PUBLISHED)) {
                throw new UpdateEventException("Only not published event can be published");
            }
        }
        return event;
    }

    @Override
    public List<Event> getByIds(List<Long> ids) {
        return eventRepository.findAllByIdIn(ids);
    }

    private void validateNewEvent(Long userId, NewEventDto newEventDto) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.plusHours(2).isAfter(newEventDto.getEventDate())) {
            String errorMessage = String.format("Field: eventDate. Error: must be at least " +
                    "2 hours after now. Value: %s", newEventDto.getEventDate());
            throw new ValidationException(errorMessage);
        }
        if (newEventDto.getParticipantLimit() < 0) {
            String errorMessage = String.format("Field: participantLimit. Error: must be at least " +
                    "0. Value: %s", newEventDto.getParticipantLimit());
            throw new ValidationException(errorMessage);
        }
    }

    private Event validateUpdateEventUser(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = getEventEntity(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User with id " + userId + " does not have an event with id " + eventId);
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new UpdateRequestException("Only pending or canceled events can be changed");
        }
        if (request.getParticipantLimit() != null && request.getParticipantLimit() < 0) {
            String errorMessage = String.format("Field: participantLimit. Error: must be at least " +
                    "0. Value: %s", request.getParticipantLimit());
            throw new ValidationException(errorMessage);
        }
        if (request.getEventDate() != null && LocalDateTime.now().plusHours(2).isAfter(request.getEventDate())) {
            String errorMessage = String.format("Field: eventDate. Error: must be at least " +
                    "2 hours after now. Value: %s", request.getEventDate());
            throw new ValidationException(errorMessage);
        }
        return event;
    }

}
