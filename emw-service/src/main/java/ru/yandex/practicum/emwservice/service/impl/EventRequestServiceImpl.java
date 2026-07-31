package ru.yandex.practicum.emwservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.event.EventRequestStatusUpdateRequest;
import ru.yandex.practicum.emwservice.dtos.event.EventRequestStatusUpdateResult;
import ru.yandex.practicum.emwservice.dtos.mappers.RequestMapper;
import ru.yandex.practicum.emwservice.dtos.request.ParticipationRequestDto;
import ru.yandex.practicum.emwservice.exception.NotFoundException;
import ru.yandex.practicum.emwservice.exception.UpdateRequestException;
import ru.yandex.practicum.emwservice.model.Event;
import ru.yandex.practicum.emwservice.model.Request;
import ru.yandex.practicum.emwservice.model.util.RequestState;
import ru.yandex.practicum.emwservice.model.util.RequestUpdateStatus;
import ru.yandex.practicum.emwservice.repository.EventRepository;
import ru.yandex.practicum.emwservice.repository.RequestRepository;
import ru.yandex.practicum.emwservice.service.interfaces.EventRequestService;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventRequestServiceImpl implements EventRequestService {

    private final UserService userService;
    private final EventRepository eventRepository;
    private final RequestRepository requestRepository;

    public EventRequestServiceImpl(UserService userService, EventRepository eventRepository, RequestRepository requestRepository) {
        this.userService = userService;
        this.eventRepository = eventRepository;
        this.requestRepository = requestRepository;
    }

    @Override
    public List<ParticipationRequestDto> getRequestsForEventByUser(Long userId, Long eventId) {
        checkUserAndEvent(userId, eventId);
        List<Request> requests = requestRepository.findRequestsByEventId(eventId);
        if (requests.isEmpty()) {
            return new ArrayList<>();
        }
        return RequestMapper.requestsToDtos(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequests(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest request) {
        Event event = checkUserAndEvent(userId, eventId);
        List<Request> requests = requestRepository.findAllById(request.getRequestIds());

        //only pending
        boolean hasNonPending = requests.stream()
                .anyMatch(req -> !req.getStatus().equals(RequestState.PENDING));
        if (hasNonPending) {
            throw new UpdateRequestException("Only pending requests can be changed");
        }

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        //cancel all if required
        if (request.getStatus().equals(RequestUpdateStatus.REJECTED)) {
            requests.forEach(req -> req.setStatus(RequestState.REJECTED));
            rejected.addAll(requests);
        } else {
            //check limit
            int limit = event.getParticipantLimit();
            int currentConfirmed = requestRepository.countByEventIdAndStatus(eventId, RequestState.CONFIRMED);
            if (limit != 0 && currentConfirmed >= limit) {
                throw new UpdateRequestException("The participant limit has been reached");
            }

            //approve until over the limit
            for (Request req : requests) {
                if (limit == 0 || currentConfirmed < limit) {
                    req.setStatus(RequestState.CONFIRMED);
                    confirmed.add(req);
                    currentConfirmed++;
                } else {
                    req.setStatus(RequestState.REJECTED);
                    rejected.add(req);
                }
            }

            //cancel all others if exists
            if (limit != 0 && currentConfirmed >= limit) {
                List<Request> pendingRequests = requestRepository.findAllByEventIdAndStatus(eventId, RequestState.PENDING);
                for (Request pendingRequest : pendingRequests) {
                    pendingRequest.setStatus(RequestState.REJECTED);
                    rejected.add(pendingRequest);
                }
                requestRepository.saveAll(pendingRequests);
            }
        }

        requestRepository.saveAll(requests);
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(RequestMapper.requestsToDtos(confirmed));
        result.setRejectedRequests(RequestMapper.requestsToDtos(rejected));
        return result;
    }

    private Event checkUserAndEvent(Long userId, Long eventId) {
        userService.isUserExist(userId);
        Optional<Event> eventOpt = eventRepository.findById(eventId);
        if (eventOpt.isEmpty()) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
        Event event = eventOpt.get();
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("User with id " + userId + " does not have an event with id " + eventId);
        }
        return event;
    }

}
