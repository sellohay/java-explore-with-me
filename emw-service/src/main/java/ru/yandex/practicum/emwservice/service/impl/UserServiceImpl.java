package ru.yandex.practicum.emwservice.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.mappers.UserMapper;
import ru.yandex.practicum.emwservice.dtos.user.NewUserRequest;
import ru.yandex.practicum.emwservice.dtos.user.UserDto;
import ru.yandex.practicum.emwservice.exception.NotFoundException;
import ru.yandex.practicum.emwservice.model.User;
import ru.yandex.practicum.emwservice.model.util.projections.UserRatingCount;
import ru.yandex.practicum.emwservice.repository.RatingRepository;
import ru.yandex.practicum.emwservice.repository.UserRepository;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    public UserServiceImpl(UserRepository userRepository, RatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
    }

    @Override
    @Transactional
    public UserDto createUser(NewUserRequest newUserRequest) {
        User newUser = UserMapper.newUserRequestToUser(newUserRequest);
        newUser = userRepository.save(newUser);
        UserDto dto = UserMapper.userToUserDto(newUser);
        dto.setRating(0);
        return dto;
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
    public List<UserDto> getUsers(List<Long> ids, int from, int size) {
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAllWithLimits(from, size);
        } else {
            users = userRepository.findAllByIdInAndLimits(ids, from, size);
        }
        return mapToUserDtoList(users);
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

    @Override
    public List<UserDto> getTopUsers(int from, int size) {
        List<User> users = userRepository.findTopUsers(from, size);
        return mapToUserDtoList(users);
    }

    private List<UserDto> mapToUserDtoList(List<User> users) {
        if (users.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = users.stream()
                .map(User::getId)
                .toList();
        Map<Long, Integer> ratingsMap = ratingRepository.getAuthorsRates(userIds)
                .stream()
                .collect(Collectors.toMap(
                        UserRatingCount::getUserId,
                        UserRatingCount::getRating
                ));
        return users.stream()
                .map(user -> {
                    UserDto dto = UserMapper.userToUserDto(user);
                    int userRating = ratingsMap.getOrDefault(user.getId(), 0);
                    dto.setRating(userRating);
                    return dto;
                })
                .toList();
    }
}
