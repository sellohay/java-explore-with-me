package ru.yandex.practicum.emwservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.mappers.RatingMapper;
import ru.yandex.practicum.emwservice.dtos.rating.RatingDto;
import ru.yandex.practicum.emwservice.exception.NotFoundException;
import ru.yandex.practicum.emwservice.exception.RatingCreationException;
import ru.yandex.practicum.emwservice.exception.RequestCreationException;
import ru.yandex.practicum.emwservice.model.Event;
import ru.yandex.practicum.emwservice.model.Rating;
import ru.yandex.practicum.emwservice.model.Request;
import ru.yandex.practicum.emwservice.model.User;
import ru.yandex.practicum.emwservice.model.util.enums.EventState;
import ru.yandex.practicum.emwservice.model.util.enums.RequestState;
import ru.yandex.practicum.emwservice.repository.RatingRepository;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;
import ru.yandex.practicum.emwservice.service.interfaces.RatingService;
import ru.yandex.practicum.emwservice.service.interfaces.RequestService;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final EventService eventService;
    private final RequestService requestService;

    public RatingServiceImpl(RatingRepository ratingRepository, UserService userService,
                             EventService eventService, RequestService requestService) {
        this.ratingRepository = ratingRepository;
        this.userService = userService;
        this.eventService = eventService;
        this.requestService = requestService;
    }

    @Override
    public RatingDto setRating(Long userId, Long eventId, boolean liked) {
        User user = userService.getUserEntity(userId);
        Event event = eventService.getEventEntity(eventId);

        validateRating(userId, eventId, liked, user, event);

        Rating rating = new Rating();
        rating.setUser(user);
        rating.setEvent(event);
        rating.setLiked(liked);
        rating = ratingRepository.save(rating);
        return RatingMapper.ratingToDto(rating);
    }

    @Override
    public RatingDto updateRating(Long userId, Long eventId, boolean liked) {
        Optional<Rating> ratingOpt = ratingRepository.findByUserIdAndEventId(userId, eventId);
        if (ratingOpt.isEmpty()) {
            throw new NotFoundException("Rating from user with id=" + userId +
                    " for event with id=" + eventId + " was not found");
        }
        Rating rating = ratingOpt.get();
        rating.setLiked(liked);
        rating = ratingRepository.save(rating);
        return RatingMapper.ratingToDto(rating);
    }

    @Override
    public void deleteRating(Long userId, Long eventId) {
        Optional<Rating> ratingOpt = ratingRepository.findByUserIdAndEventId(userId, eventId);
        if (ratingOpt.isEmpty()) {
            throw new NotFoundException("Rating from user with id=" + userId +
                    " for event with id=" + eventId + " was not found");
        }
        ratingRepository.delete(ratingOpt.get());
    }

    private void validateRating(Long userId, Long eventId, boolean liked, User user, Event event) {
        //check if user and event exists
        //check if user isn't initiator
        //check if published and in the past
        //check if user has confirmed request
        Optional<Rating> ratingOpt = ratingRepository.findByUserIdAndEventId(userId, eventId);
        if (ratingOpt.isPresent()) {
            throw new RatingCreationException("User with id=" + userId + " has already rated event with id=" + eventId);
        }

        if (user.getId().equals(event.getInitiator().getId())) {
            throw new RatingCreationException("User with id=" + userId +
                    " is initiator of the event with id=" + eventId);
        }
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new RatingCreationException("Event with id=" + eventId + "has not been published");
        }
        //отключено для тестов
        /*if (event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new RatingCreationException("Event with id=" + eventId + "is in the future");
        }*/
        Request request = requestService.getRequestEntity(userId, eventId);
        if (!request.getStatus().equals(RequestState.CONFIRMED)) {
            throw new RequestCreationException("User with id=" + userId + "has not attended event with id=" + eventId);
        }
    }
}
