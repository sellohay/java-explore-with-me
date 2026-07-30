package ru.yandex.practicum.emwservice.controller.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.emwservice.dtos.event.EventFullDto;
import ru.yandex.practicum.emwservice.dtos.event.UpdateEventAdminRequest;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventFullDto> getEvents(
        @RequestParam(required = false) List<Long> users,
        @RequestParam(required = false) List<String> states,
        @RequestParam(required = false) List<Long> categories,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
        @RequestParam(required = false, defaultValue = "0") int from,
        @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return eventService.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEventAdmin(@PathVariable Long eventId, @RequestBody UpdateEventAdminRequest request) {
        return eventService.updateEventAdmin(eventId, request);
    }
}
