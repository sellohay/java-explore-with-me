package ru.yandex.practicum.emwservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.emwservice.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    Compilation save(Compilation compilation);

    boolean existsById(Long id);

    void deleteById(Long id);

    @Query(value = "SELECT * FROM compilations LIMIT :size OFFSET :from", nativeQuery = true)
    List<Compilation> findCompilationWithFilters(int from, int size);

    @Query(value = """
        SELECT * FROM compilations c
        WHERE c.pinned = :pinned
        LIMIT :size
        OFFSET :from
    """, nativeQuery = true)
    List<Compilation> findCompilationsWithPinnedFilter(Boolean pinned, int from, int size);
}
