package ru.yandex.practicum.emwservice.dtos.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.dtos.category.CategoryDto;
import ru.yandex.practicum.emwservice.dtos.user.UserShortDto;

@Getter
@Setter
@ToString
public class EventShortDto {

    private Long id;
    private String annotation;

    private CategoryDto category;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int confirmedRequests;

    private String eventDate;

    private UserShortDto initiator;

    private boolean paid;

    private String title;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long views;
}
