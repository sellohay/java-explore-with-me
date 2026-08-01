package ru.yandex.practicum.emwservice.controller.privates;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.emwservice.dtos.event.*;
import ru.yandex.practicum.emwservice.dtos.request.ParticipationRequestDto;
import ru.yandex.practicum.emwservice.service.interfaces.EventRequestService;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {

    private final EventService eventService;
    private final EventRequestService eventRequestService;

    public PrivateEventController(EventService eventService, EventRequestService eventRequestService) {
        this.eventService = eventService;
        this.eventRequestService = eventRequestService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping
    public List<EventShortDto> getEvents(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return eventService.getEventsByUser(userId, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByUser(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventByUser(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventUserRequest request) {
        return eventService.updateEventByUser(userId, eventId, request);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventRequestService.getRequestsForEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateEventRequestStatus(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest request
    ) {
        return eventRequestService.updateEventRequests(userId, eventId, request);
    }
}
