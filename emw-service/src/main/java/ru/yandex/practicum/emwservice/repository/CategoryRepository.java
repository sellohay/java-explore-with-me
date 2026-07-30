package ru.yandex.practicum.emwservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.emwservice.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category save (Category category);

    boolean existsById(Long id);

    void deleteById(Long id);

    Optional<Category> findById(Long id);

    @Query(value = "SELECT * FROM categories LIMIT :size OFFSET :from", nativeQuery = true)
    List<Category> findAllWithLimits(int from, int size);
}
