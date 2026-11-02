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

    @Query(value = """
        SELECT u.*
        FROM users u
        LEFT JOIN events e ON u.id = e.initiator_id AND e.state = 'PUBLISHED'
        LEFT JOIN ratings r ON e.id = r.event_id
        GROUP BY u.id
        ORDER BY COALESCE(SUM(CASE WHEN r.liked = true THEN 1 WHEN r.liked = false THEN -1 ELSE 0 END), 0) DESC, u.id ASC
        LIMIT :size
        OFFSET :from
    """, nativeQuery = true)
    List<User> findTopUsers(int from, int size);
}
