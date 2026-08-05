package ru.yandex.practicum.emwservice.dtos.compilation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.yandex.practicum.emwservice.dtos.event.EventShortDto;

import java.util.List;

@Getter
@Setter
@ToString
public class CompilationDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<EventShortDto> events;
    private Long id;
    private Boolean pinned;
    private String title;
}
