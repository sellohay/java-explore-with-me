package ru.yandex.practicum.emwservice.dtos.rating;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RatingDto {
    private Long id;
    private Long userId;
    private Long eventId;
    private boolean liked;
}
