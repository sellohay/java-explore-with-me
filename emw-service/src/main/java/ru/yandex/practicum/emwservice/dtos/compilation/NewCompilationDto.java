package ru.yandex.practicum.emwservice.dtos.compilation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class NewCompilationDto {

    @JsonProperty("events")
    private List<Long> eventIds;

    private Boolean pinned = false;

    @NotBlank(message = "Title can't be empty")
    @Size(min = 1, max = 50, message = "Title length must be from 1 to 50 symbols")
    private String title;
}
