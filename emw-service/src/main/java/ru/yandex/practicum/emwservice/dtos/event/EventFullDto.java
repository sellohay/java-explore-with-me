package ru.yandex.practicum.emwservice.dtos.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.dtos.category.CategoryDto;
import ru.yandex.practicum.emwservice.dtos.user.UserShortDto;
import ru.yandex.practicum.emwservice.model.util.Location;


@Getter
@Setter
@ToString
public class EventFullDto {

    private Long id;
    private String annotation;
    private CategoryDto category;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer confirmedRequests;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createdOn;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    private String eventDate;

    private UserShortDto initiator;
    private Location location;
    private boolean paid;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer participantLimit;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String publishedOn;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean requestModeration;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String state;

    private String title;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long views;
}
