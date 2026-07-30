package ru.yandex.practicum.emwservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.mappers.UserMapper;
import ru.yandex.practicum.emwservice.dtos.user.NewUserRequest;
import ru.yandex.practicum.emwservice.dtos.user.UserDto;
import ru.yandex.practicum.emwservice.exception.NotFoundException;
import ru.yandex.practicum.emwservice.model.User;
import ru.yandex.practicum.emwservice.repository.UserRepository;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        User newUser = UserMapper.newUserRequestToUser(newUserRequest);
        newUser = userRepository.save(newUser);
        return UserMapper.userToUserDto(newUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User with id=" + id + "was not found");
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAllWithLimits(from, size);
        } else {
            users = userRepository.findAllByIdInAndLimits(ids, from, size);
        }
        if (users.isEmpty()) {
            return new ArrayList<>();
        }
        return UserMapper.usersToUserDtos(users);
    }

    @Override
    public boolean isUserExist(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public User getUserEntity(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new NotFoundException("User with id=" + id + "was not found"));
    }

}
