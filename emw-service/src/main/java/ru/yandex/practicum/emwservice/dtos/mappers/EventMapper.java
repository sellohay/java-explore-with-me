package ru.yandex.practicum.emwservice.dtos.mappers;

import ru.yandex.practicum.emwservice.dtos.event.*;
import ru.yandex.practicum.emwservice.model.Event;
import ru.yandex.practicum.emwservice.model.util.EventState;
import ru.yandex.practicum.emwservice.model.util.StateAction;
import ru.yandex.practicum.emwservice.model.util.StateActionAdmin;

import java.time.format.DateTimeFormatter;

public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event newToEvent(NewEventDto newEventDto) {
        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(newEventDto.getEventDate());
        event.setLocation(newEventDto.getLocation());
        event.setPaid(newEventDto.isPaid());
        event.setParticipantLimit(newEventDto.getParticipantLimit());
        event.setRequestModeration(newEventDto.isRequestModeration());
        event.setState(EventState.PENDING);
        event.setTitle(newEventDto.getTitle());
        return event;
    }

    public static EventFullDto eventToEventFullDto(Event event) {
        EventFullDto eventFullDto = new EventFullDto();
        eventFullDto.setId(event.getId());
        eventFullDto.setAnnotation(event.getAnnotation());
        eventFullDto.setCategory(CategoryMapper.categoryToDto(event.getCategory()));
        eventFullDto.setCreatedOn(event.getCreatedOn().format(FORMATTER));
        eventFullDto.setDescription(event.getDescription());
        eventFullDto.setEventDate(event.getEventDate().format(FORMATTER));
        eventFullDto.setInitiator(UserMapper.userToUserShortDto(event.getInitiator()));
        eventFullDto.setLocation(event.getLocation());
        eventFullDto.setPaid(event.isPaid());
        eventFullDto.setParticipantLimit(event.getParticipantLimit());

        if (event.getPublishedOn() != null) {
            eventFullDto.setPublishedOn(event.getPublishedOn().format(FORMATTER));
        }
        eventFullDto.setRequestModeration(event.isRequestModeration());
        eventFullDto.setState(event.getState().name());
        eventFullDto.setTitle(event.getTitle());
        return eventFullDto;
    }

    public static EventShortDto eventToEventShortDto(Event event) {
        EventShortDto eventShortDto = new EventShortDto();
        eventShortDto.setId(event.getId());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setCategory(CategoryMapper.categoryToDto(event.getCategory()));
        eventShortDto.setEventDate(event.getEventDate().format(FORMATTER));
        eventShortDto.setInitiator(UserMapper.userToUserShortDto(event.getInitiator()));
        eventShortDto.setPaid(event.isPaid());
        eventShortDto.setTitle(event.getTitle());
        return eventShortDto;
    }

    public static Event updateEventFieldsAdmin(Event event, UpdateEventAdminRequest request) {
        event = updateEventFields(event, request);
        if (request.getStateAction() != null) {
            if (request.getStateAction().equals(StateActionAdmin.PUBLISH_EVENT)) {
                event.setState(EventState.PUBLISHED);
            } else {
                event.setState(EventState.CANCELLED);
            }
        }
        return event;
    }

    public static Event updateEventFieldsUser(Event event, UpdateEventUserRequest request) {
        event = updateEventFields(event, request);
        if (request.getStateAction() != null) {
            if (request.getStateAction().equals(StateAction.CANCEL_REVIEW)) {
                event.setState(EventState.CANCELLED);
            } else {
                event.setState(EventState.PENDING);
            }
        }
        return event;
    }

    private static Event updateEventFields(Event event, UpdateEventBaseRequest request) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
        return event;
    }
}
