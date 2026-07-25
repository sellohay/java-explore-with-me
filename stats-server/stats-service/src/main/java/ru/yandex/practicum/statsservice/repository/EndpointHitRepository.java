package ru.yandex.practicum.statsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.statsdto.dtos.ViewStatsDto;
import ru.yandex.practicum.statsservice.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    EndpointHit save(EndpointHit endpointHit);

    @Query("""
    SELECT new ru.yandex.practicum.statsdto.dtos.ViewStatsDto(e.app, e.uri, COUNT(e.ip))
    FROM EndpointHit e
    WHERE e.timestamp BETWEEN :start AND :end
    GROUP BY e.app, e.uri
    ORDER BY COUNT(e.ip) DESC
    """)
    List<ViewStatsDto> getStatsNoUrisNotUnique(LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT new ru.yandex.practicum.statsdto.dtos.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
    FROM EndpointHit e
    WHERE e.timestamp BETWEEN :start AND :end
    GROUP BY e.app, e.uri
    ORDER BY COUNT(DISTINCT e.ip) DESC
    """)
    List<ViewStatsDto> getStatsNoUrisUnique(LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT new ru.yandex.practicum.statsdto.dtos.ViewStatsDto(e.app, e.uri, COUNT(e.ip))
    FROM EndpointHit e
    WHERE e.timestamp BETWEEN :start AND :end
    AND e.uri IN :uris
    GROUP BY e.app, e.uri
    ORDER BY COUNT(e.ip) DESC
    """)
    List<ViewStatsDto> getStatsUrisNotUnique(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("""
    SELECT new ru.yandex.practicum.statsdto.dtos.ViewStatsDto(e.app, e.uri, COUNT(DISTINCT e.ip))
    FROM EndpointHit e
    WHERE e.timestamp BETWEEN :start AND :end
    AND e.uri IN :uris
    GROUP BY e.app, e.uri
    ORDER BY COUNT(DISTINCT e.ip) DESC
    """)
    List<ViewStatsDto> getStatsUrisUnique(LocalDateTime start, LocalDateTime end, List<String> uris);
}
