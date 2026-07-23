package ru.yandex.practicum.statsservice.controllers;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.statsdto.dtos.EndpointHitDto;
import ru.yandex.practicum.statsdto.dtos.NewEndpointHitDto;
import ru.yandex.practicum.statsdto.dtos.ViewStatsDto;
import ru.yandex.practicum.statsservice.service.EndpointHitService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class StatsController {

    private final EndpointHitService endpointHitService;

    public StatsController(EndpointHitService endpointHitService) {
        this.endpointHitService = endpointHitService;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto hit(@Valid @RequestBody NewEndpointHitDto newEndpointHitDto) {
        return endpointHitService.createEndpointHit(newEndpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> stats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(required = false, defaultValue = "false") boolean unique
            ) {
        return endpointHitService.getStats(start, end, uris, unique);
    }
}
