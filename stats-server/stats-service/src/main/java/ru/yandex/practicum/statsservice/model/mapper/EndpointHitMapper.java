package ru.yandex.practicum.statsservice.model.mapper;

import ru.yandex.practicum.statsdto.dtos.EndpointHitDto;
import ru.yandex.practicum.statsdto.dtos.NewEndpointHitDto;
import ru.yandex.practicum.statsservice.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EndpointHitMapper {

    public static EndpointHit mapToEndpointHit(NewEndpointHitDto newEndpointHitDto) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(newEndpointHitDto.getApp());
        endpointHit.setUri(newEndpointHitDto.getUri());
        endpointHit.setIp(newEndpointHitDto.getIp());
        LocalDateTime timestamp = LocalDateTime.parse(
                newEndpointHitDto.getTimestamp(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        endpointHit.setTimestamp(timestamp);
        return endpointHit;
    }

    public static EndpointHitDto mapToEndpointHitDto(EndpointHit endpointHit) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setId(endpointHit.getId());
        endpointHitDto.setApp(endpointHit.getApp());
        endpointHitDto.setUri(endpointHit.getUri());
        endpointHitDto.setIp(endpointHit.getIp());
        endpointHitDto.setTimestamp(endpointHit.getTimestamp());
        return endpointHitDto;
    }
}
