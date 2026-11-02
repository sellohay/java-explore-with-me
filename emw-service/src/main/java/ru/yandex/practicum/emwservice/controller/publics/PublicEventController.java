package ru.yandex.practicum.emwservice.controller.publics;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.emwservice.dtos.event.EventFullDto;
import ru.yandex.practicum.emwservice.dtos.event.EventShortDto;
import ru.yandex.practicum.emwservice.model.util.enums.EventSortOption;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;
import ru.yandex.practicum.statsclient.StatsClient;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
public class PublicEventController {

    private static final String appName = "app-ewm-service";

    private final EventService eventService;
    private final StatsClient statsClient;

    public PublicEventController(EventService eventService, StatsClient statsClient) {
        this.eventService = eventService;
        this.statsClient = statsClient;
    }

    @GetMapping
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Integer> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(required = false, defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(required = false, defaultValue = "EVENT_DATE") EventSortOption sortOption,
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size,
            HttpServletRequest request) {
        List<EventShortDto> events = eventService.getEvents(text, categories, paid, rangeStart,
                rangeEnd, onlyAvailable, sortOption, from, size);
        sendHit(request);
        return events;
    }

    @GetMapping("/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        EventFullDto event = eventService.getEventById(id);
        sendHit(request);
        return event;
    }

    private void sendHit(HttpServletRequest request) {
        try {
            statsClient.saveHit(
                    appName,
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now()
            );
        } catch (Exception ignored) {
        }
    }
}
