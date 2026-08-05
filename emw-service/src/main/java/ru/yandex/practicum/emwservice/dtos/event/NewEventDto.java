package ru.yandex.practicum.emwservice.dtos.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.model.util.Location;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class NewEventDto {

    @NotBlank(message = "Annotation can't be empty")
    @Size(min = 20, max = 2000, message = "Annotation length must be from 20 to 2000 symbols")
    private String annotation;

    @NotNull(message = "Category ID is required")
    @JsonProperty("category")
    private Long categoryId;

    @NotBlank(message = "Description can't be empty")
    @Size(min = 20, max = 7000, message = "Description length must be from 20 to 7000 symbols")
    private String description;

    @NotNull(message = "Event date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull(message = "Location can't be empty")
    @Valid
    private Location location;

    private boolean paid = false;

    private int participantLimit = 0;

    private boolean requestModeration = true;

    @NotBlank(message = "Title can't be empty")
    @Size(min = 3, max = 120, message = "Title length must be from 3 to 120 symbols")
    private String title;

}

