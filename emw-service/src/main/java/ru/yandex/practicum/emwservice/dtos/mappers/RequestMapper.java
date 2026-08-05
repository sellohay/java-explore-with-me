package ru.yandex.practicum.emwservice.dtos.mappers;

import ru.yandex.practicum.emwservice.dtos.request.ParticipationRequestDto;
import ru.yandex.practicum.emwservice.model.Request;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static ParticipationRequestDto requestToDto(Request request) {
        ParticipationRequestDto dto = new ParticipationRequestDto();
        dto.setId(request.getId());
        dto.setRequesterId(request.getRequester().getId());
        dto.setEventId(request.getEvent().getId());
        dto.setCreated(request.getCreated().format(FORMATTER));
        dto.setStatus(request.getStatus().name());
        return dto;
    }

    public static List<ParticipationRequestDto> requestsToDtos(List<Request> requests) {
        List<ParticipationRequestDto> dtos = new ArrayList<>();
        for (Request request : requests) {
            dtos.add(requestToDto(request));
        }
        return dtos;
    }
}
