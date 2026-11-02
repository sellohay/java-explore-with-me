package ru.yandex.practicum.emwservice.service.interfaces;

import ru.yandex.practicum.emwservice.dtos.request.ParticipationRequestDto;
import ru.yandex.practicum.emwservice.model.Request;

import java.util.List;

public interface RequestService {

    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequests(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    Request getRequestEntity(Long requesterId, Long eventId);
}
