package ru.yandex.practicum.emwservice.dtos.mappers;

import ru.yandex.practicum.emwservice.dtos.rating.RatingDto;
import ru.yandex.practicum.emwservice.model.Rating;

public class RatingMapper {

    public static RatingDto ratingToDto(Rating rating) {
        RatingDto dto = new RatingDto();
        dto.setId(rating.getId());
        dto.setUserId(rating.getUser().getId());
        dto.setEventId(rating.getEvent().getId());
        dto.setLiked(rating.isLiked());
        return dto;
    }
}
