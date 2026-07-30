package ru.yandex.practicum.emwservice.service.interfaces;

import ru.yandex.practicum.emwservice.dtos.event.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.emwservice.dtos.event.EventRequestStatusUpdateResult;
import ru.yandex.practicum.emwservice.dtos.request.ParticipationRequestDto;

import java.util.List;

public interface EventRequestService {

    List<ParticipationRequestDto> getRequestsForEventByUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId, EventRequestStatusUpdateRequest request);
}
