package ru.yandex.practicum.emwservice.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.event.EventFullDto;
import ru.yandex.practicum.emwservice.dtos.event.EventShortDto;
import ru.yandex.practicum.emwservice.dtos.mappers.EventMapper;
import ru.yandex.practicum.emwservice.model.Event;
import ru.yandex.practicum.emwservice.model.util.projections.ConfirmedRequestsCount;
import ru.yandex.practicum.emwservice.model.util.projections.EventRatingCount;
import ru.yandex.practicum.emwservice.model.util.enums.RequestState;
import ru.yandex.practicum.emwservice.repository.EventRepository;
import ru.yandex.practicum.emwservice.repository.RatingRepository;
import ru.yandex.practicum.emwservice.repository.RequestRepository;
import ru.yandex.practicum.statsclient.StatsClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EventUtilService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;
    private final RatingRepository ratingRepository;
    private final StatsClient statsClient;

    public EventUtilService(EventRepository eventRepository, RequestRepository requestRepository,
                            RatingRepository ratingRepository, StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
        this.ratingRepository = ratingRepository;
        this.statsClient = statsClient;
    }

    public EventFullDto mapToEventFullDto(Event event) {
        Map<Long, Long> viewsMap = getViewsMap(java.util.List.of(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);
        int confReq = requestRepository.countByEventIdAndStatus(event.getId(), RequestState.CONFIRMED);
        EventRatingCount rate = ratingRepository.getEventRatingCountById(event.getId());
        int likes = (rate != null && rate.getLikes() != null ? rate.getLikes() : 0);
        int dislikes = (rate != null && rate.getDislikes() != null ? rate.getDislikes() : 0);

        EventFullDto dto = EventMapper.eventToEventFullDto(event);
        dto.setConfirmedRequests(confReq);
        dto.setViews(views);
        dto.setLikes(likes);
        dto.setDislikes(dislikes);
        dto.setRating(likes - dislikes);
        return dto;
    }

    public List<EventFullDto> mapToEventFullDtoList(List<Event> events) {
        events = addCategoryAndInitiator(events);
        Map<Long, Long> viewsMap = getViewsMap(events);
        Map<Long, Integer> confirmedRequestsMap = getConfReqMap(events);
        Map<Long, EventRatingCount> ratingsMap = getRatingsMap(events);

        return events.stream()
                .map(event -> {
                    int confReq = confirmedRequestsMap.getOrDefault(event.getId(), 0);
                    long views = viewsMap.getOrDefault(event.getId(), 0L);

                    EventRatingCount rate = ratingsMap.get(event.getId());
                    int likes = (rate != null && rate.getLikes() != null ? rate.getLikes() : 0);
                    int dislikes = (rate != null && rate.getDislikes() != null ? rate.getDislikes() : 0);
                    int rating = likes - dislikes;
                    return mapToEventFullDto(event, confReq, views, likes, dislikes, rating);
                })
                .toList();
    }

    public List<EventShortDto> mapToEventShortDtoList(List<Event> events) {
        events = addCategoryAndInitiator(events);
        Map<Long, Long> viewsMap = getViewsMap(events);
        Map<Long, Integer> confirmedRequestsMap = getConfReqMap(events);
        Map<Long, EventRatingCount> ratingsMap = getRatingsMap(events);

        return events.stream()
                .map(event -> {
                    int confReq = confirmedRequestsMap.getOrDefault(event.getId(), 0);
                    long views = viewsMap.getOrDefault(event.getId(), 0L);

                    EventRatingCount rate = ratingsMap.get(event.getId());
                    int likes = (rate != null && rate.getLikes() != null ? rate.getLikes() : 0);
                    int dislikes = (rate != null && rate.getDislikes() != null ? rate.getDislikes() : 0);
                    int rating = likes - dislikes;
                    return mapToEventShortDto(event, confReq, views, rating);
                })
                .toList();

    }

    public Map<Long, Long> getViewsMap(List<Event> events) {
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

        return getStatsMap(start, end, uris);
    }

    public Map<Long, Long> getStatsMap(LocalDateTime start, LocalDateTime end, List<String> uris) {
        String startStr = start.format(formatter);
        String endStr = end.format(formatter);
        ResponseEntity<Object> response = statsClient.getStats(startStr, endStr, uris, true);
        Object body = response.getBody();

        Map<Long, Long> viewsMap = new HashMap<>();
        if (body instanceof List) {
            List<Map<String, Object>> statsList = (List<Map<String, Object>>) body;

            for (Map<String, Object> stat : statsList) {
                String uri = (String) stat.get("uri");
                Number hitsNumber = (Number) stat.get("hits");
                Long hits = hitsNumber != null ? hitsNumber.longValue() : 0L;

                if (uri != null && uri.startsWith("/events/")) {
                    Long eventId = Long.parseLong(uri.replace("/events/", ""));
                    viewsMap.put(eventId, hits);
                }
            }
        }
        return viewsMap;
    }

    public Map<Long, Integer> getConfReqMap(List<Event> events) {
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

    public Map<Long, EventRatingCount> getRatingsMap(List<Event> events) {
        if (events.isEmpty()) {
            return new HashMap<>();
        }
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        return ratingRepository.getEventRatingsByIds(eventIds)
                .stream()
                .collect(Collectors.toMap(
                        EventRatingCount::getEventId,
                        count -> count
                ));
    }


    public List<Event> addCategoryAndInitiator(List<Event> events) {
        if (events.isEmpty()) {
            return events;
        }
        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();
        Map<Long, Event> eventsMap = eventRepository.findAllByIdIn(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, event -> event));
        return events.stream()
                .map(event -> eventsMap.get(event.getId()))
                .toList();
    }


    public EventFullDto mapToEventFullDto(Event event, int confReq, Long views, int likes, int dislikes, int rating) {
        EventFullDto dto = EventMapper.eventToEventFullDto(event);
        dto.setConfirmedRequests(confReq);
        dto.setViews(views);
        dto.setLikes(likes);
        dto.setDislikes(dislikes);
        dto.setRating(rating);
        return dto;
    }

    public EventShortDto mapToEventShortDto(Event event, int confReq, long views, int rating) {
        EventShortDto dto = EventMapper.eventToEventShortDto(event);
        dto.setConfirmedRequests(confReq);
        dto.setViews(views);
        dto.setRating(rating);
        return dto;
    }

}
