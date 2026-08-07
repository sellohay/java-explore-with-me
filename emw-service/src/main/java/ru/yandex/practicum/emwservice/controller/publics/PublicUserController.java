package ru.yandex.practicum.emwservice.controller.publics;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.emwservice.dtos.user.UserDto;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class PublicUserController {

    private final UserService userService;

    public PublicUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/rating")
    public List<UserDto> getTopUsers(
            @RequestParam(required = false, defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(required = false, defaultValue = "10") @Positive int size
    ) {
        return userService.getTopUsers(from, size);
    }
}
