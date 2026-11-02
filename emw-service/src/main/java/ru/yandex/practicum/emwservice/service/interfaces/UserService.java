package ru.yandex.practicum.emwservice.service.interfaces;

import ru.yandex.practicum.emwservice.dtos.user.NewUserRequest;
import ru.yandex.practicum.emwservice.dtos.user.UserDto;
import ru.yandex.practicum.emwservice.model.User;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);

    void deleteUser(Long id);

    List<UserDto> getUsers(List<Long> ids, int from, int size);

    boolean isUserExist(Long id);

    User getUserEntity(Long id);

    List<UserDto> getTopUsers(int from, int size);
}
