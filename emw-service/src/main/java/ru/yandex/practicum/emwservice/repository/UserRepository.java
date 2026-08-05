package ru.yandex.practicum.emwservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.emwservice.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User save(User user);

    void deleteById(Long id);

    boolean existsById(Long id);

    User findById(long id);

    @Query(value = "SELECT * FROM users LIMIT :size OFFSET :from", nativeQuery = true)
    List<User> findAllWithLimits(int from, int size);

    @Query(value = "SELECT * FROM users WHERE id IN :ids LIMIT :size OFFSET :from", nativeQuery = true)
    List<User> findAllByIdInAndLimits(List<Long> ids, int from, int size);
}
