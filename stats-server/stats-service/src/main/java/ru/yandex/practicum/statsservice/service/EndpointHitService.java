package ru.yandex.practicum.statsservice.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.statsdto.dtos.EndpointHitDto;
import ru.yandex.practicum.statsdto.dtos.NewEndpointHitDto;
import ru.yandex.practicum.statsdto.dtos.ViewStatsDto;
import ru.yandex.practicum.statsservice.model.EndpointHit;
import ru.yandex.practicum.statsservice.model.mapper.EndpointHitMapper;
import ru.yandex.practicum.statsservice.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EndpointHitService {

    private final EndpointHitRepository endpointHitRepository;

    public EndpointHitService(EndpointHitRepository endpointHitRepository) {
        this.endpointHitRepository = endpointHitRepository;
    }

    public EndpointHitDto createEndpointHit(NewEndpointHitDto newEndpointHitDto) {
        EndpointHit newHit = EndpointHitMapper.mapToEndpointHit(newEndpointHitDto);
        newHit = endpointHitRepository.save(newHit);
        return EndpointHitMapper.mapToEndpointHitDto(newHit);
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return endpointHitRepository.getStatsNoUrisUnique(start, end);
            }
            return endpointHitRepository.getStatsNoUrisNotUnique(start, end);
        }
        if (unique) {
            return endpointHitRepository.getStatsUrisUnique(start, end, uris);
        }
        return endpointHitRepository.getStatsUrisNotUnique(start, end, uris);
    }
}
