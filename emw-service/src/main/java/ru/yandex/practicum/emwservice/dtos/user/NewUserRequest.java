package ru.yandex.practicum.emwservice.dtos.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NewUserRequest {

    @NotBlank(message = "Email can't be empty")
    @Size(min = 6, max = 254, message = "Email length must be from 6 to 254 symbols")
    @Email(message = "Email must be correct")
    private String email;

    @NotBlank(message = "Name can't be empty")
    @Size(min = 2, max = 250, message = "Name length must be from 2 to 250 symbols")
    private String name;
}
