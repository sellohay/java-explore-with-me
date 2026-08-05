package ru.yandex.practicum.emwservice.dtos.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewCategoryDto {

    @NotBlank(message = "Name can't be empty")
    @Size(min = 1, max = 50, message = "Name length must be from 1 to 50 symbols")
    private String name;
}
