package ru.yandex.practicum.emwservice.dtos.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDto {
    private String email;
    private Long id;
    private String name;
    private int rating;
}
