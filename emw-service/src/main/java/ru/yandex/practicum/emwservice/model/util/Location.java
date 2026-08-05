package ru.yandex.practicum.emwservice.model.util;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Location {

    @NotNull(message = "Latitude can't be empty")
    @Min(value = -90, message = "Latitude must be greater than -90")
    @Max(value = 90, message = "Latitude must be less than 90")
    private Double lat;

    @NotNull(message = "Longtitude can't be empty")
    @Min(value = -180, message = "Longtitude must be greater than -180")
    @Max(value = 180, message = "Longtitude must be less than 180")
    private Double lon;

}
