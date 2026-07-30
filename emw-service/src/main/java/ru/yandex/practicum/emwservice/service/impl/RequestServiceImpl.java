package ru.yandex.practicum.emwservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.mappers.RequestMapper;
import ru.yandex.practicum.emwservice.dtos.request.ParticipationRequestDto;
import ru.yandex.practicum.emwservice.exception.NotFoundException;
import ru.yandex.practicum.emwservice.exception.RequestCreationException;
import ru.yandex.practicum.emwservice.model.Event;
import ru.yandex.practicum.emwservice.model.Request;
import ru.yandex.practicum.emwservice.model.User;
import ru.yandex.practicum.emwservice.model.util.EventState;
import ru.yandex.practicum.emwservice.model.util.RequestState;
import ru.yandex.practicum.emwservice.repository.RequestRepository;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;
import ru.yandex.practicum.emwservice.service.interfaces.RequestService;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final EventService eventService;
    private final UserService userService;

    public RequestServiceImpl(RequestRepository requestRepository, EventService eventService, UserService userService) {
        this.requestRepository = requestRepository;
        this.eventService = eventService;
        this.userService = userService;
    }

    @Override
    @Transactional
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        User user = userService.getUserEntity(userId);
        Event event = eventService.getEventEntity(eventId);

        //check if there's already request
        //check if the same user
        //check if published
        //check if over the limit
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new RequestCreationException("Request already exists");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new RequestCreationException("User can't create request to their own event");
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new RequestCreationException("Event hasn't been published yet");
        }
        int limit = event.getParticipantLimit();
        if (limit != 0) {
            //check how many participants
            if (requestRepository.countByEventIdAndStatus(eventId, RequestState.CONFIRMED) == limit) {
                throw new RequestCreationException("Participant limit exceeded");
            }
        }

        Request request = new Request();
        request.setRequester(user);
        request.setEvent(event);
        request.setCreated(LocalDateTime.now());
        //after creation check for required moderation
        if (event.isRequestModeration()) {
            request.setStatus(RequestState.PENDING);
        } else {
            request.setStatus(RequestState.CONFIRMED);
        }
        request = requestRepository.save(request);

        return RequestMapper.requestToDto(request);
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("User with id=" + userId + " does not exist");
        }
        List<Request> requests = requestRepository.findByRequesterId(userId);
        return RequestMapper.requestsToDtos(requests);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        if (!userService.isUserExist(userId)) {
            throw new NotFoundException("User with id=" + userId + " does not exist");
        }
        Optional<Request> reqOpt = requestRepository.findById(requestId);
        if (reqOpt.isEmpty()) {
            throw new NotFoundException("Request with id=" + requestId + " does not exist");
        }
        Request request = reqOpt.get();
        request.setStatus(RequestState.CANCELLED);
        requestRepository.save(request);
        return RequestMapper.requestToDto(request);
    }
}
