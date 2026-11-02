package ru.yandex.practicum.emwservice.service.interfaces;

import ru.yandex.practicum.emwservice.dtos.rating.RatingDto;

public interface RatingService {
    RatingDto setRating(Long userId, Long eventId, boolean liked);

    RatingDto updateRating(Long userId, Long eventId, boolean liked);

    void deleteRating(Long userId, Long eventId);
}
