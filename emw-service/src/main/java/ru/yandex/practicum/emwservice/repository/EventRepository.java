package ru.yandex.practicum.emwservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.emwservice.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    Event save(Event event);

    boolean existsByCategoryId(Long categoryId);

    Event findById(long id);

    @Query(value = """
        SELECT e.* FROM events e
        WHERE e.state = 'PUBLISHED'
          AND (:text IS NULL
               OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', CAST(:text AS varchar), '%'))
               OR LOWER(e.description) LIKE LOWER(CONCAT('%', CAST(:text AS varchar), '%')))
          AND (:hasCategories = FALSE OR e.category_id IN (:categories))
          AND (:paid IS NULL OR e.paid = :paid)
          AND (
                (CAST(:rangeStart AS timestamp) IS NULL AND CAST(:rangeEnd AS timestamp) IS NULL AND e.event_date > CURRENT_TIMESTAMP)
                OR (CAST(:rangeStart AS timestamp) IS NOT NULL AND CAST(:rangeEnd AS timestamp) IS NOT NULL AND e.event_date BETWEEN :rangeStart AND :rangeEnd)
                OR (CAST(:rangeStart AS timestamp) IS NOT NULL AND CAST(:rangeEnd AS timestamp) IS NULL AND e.event_date >= :rangeStart)
                OR (CAST(:rangeStart AS timestamp) IS NULL AND CAST(:rangeEnd AS timestamp) IS NOT NULL AND e.event_date <= :rangeEnd)
              )
          AND (:onlyAvailable = FALSE
               OR e.participant_limit = 0
               OR e.participant_limit > (
                   SELECT COUNT(r.id) FROM requests r
                   WHERE r.event_id = e.id
                     AND r.status = 'CONFIRMED'
               ))
        ORDER BY e.event_date DESC
        LIMIT :size
        OFFSET :from
        """, nativeQuery = true)
    List<Event> findPublishedWithFiltersSortEventDate(String text, Boolean hasCategories, List<Integer> categories, Boolean paid,
                                                      LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                      Boolean onlyAvailable, int from, int size);

    @Query(value = """
        SELECT e.* FROM events e
        WHERE e.state = 'PUBLISHED'
          AND (:text IS NULL
               OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', CAST(:text AS varchar), '%'))
               OR LOWER(e.description) LIKE LOWER(CONCAT('%', CAST(:text AS varchar), '%')))
          AND (:hasCategories = FALSE OR e.category_id IN (:categories))
          AND (:paid IS NULL OR e.paid = :paid)
          AND (
                (CAST(:rangeStart AS timestamp) IS NULL AND CAST(:rangeEnd AS timestamp) IS NULL AND e.event_date > CURRENT_TIMESTAMP)
                OR (CAST(:rangeStart AS timestamp) IS NOT NULL AND CAST(:rangeEnd AS timestamp) IS NOT NULL AND e.event_date BETWEEN :rangeStart AND :rangeEnd)
                OR (CAST(:rangeStart AS timestamp) IS NOT NULL AND CAST(:rangeEnd AS timestamp) IS NULL AND e.event_date >= :rangeStart)
                OR (CAST(:rangeStart AS timestamp) IS NULL AND CAST(:rangeEnd AS timestamp) IS NOT NULL AND e.event_date <= :rangeEnd)
              )
          AND (:onlyAvailable = FALSE
               OR e.participant_limit = 0
               OR e.participant_limit > (
                   SELECT COUNT(r.id) FROM requests r
                   WHERE r.event_id = e.id
                     AND r.status = 'CONFIRMED'
               ))
        """, nativeQuery = true)
    List<Event> findPublishedWithFiltersSortViews(String text, Boolean hasCategories, List<Integer> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable);

    @Query(value = "SELECT * FROM events WHERE initiator_id = :userId LIMIT :size OFFSET :from", nativeQuery = true)
    List<Event> getEventsByUser(Long userId, int from, int size);

    @Query(value = """
        SELECT * FROM events
        WHERE (:hasUsers = FALSE OR initiator_id IN (:users))
          AND (:hasStates = FALSE OR state IN (:states))
          AND (:hasCategories = FALSE OR category_id IN (:categories))
          AND (CAST(:rangeStart AS timestamp) IS NULL OR event_date >= CAST(:rangeStart AS timestamp))
          AND (CAST(:rangeEnd AS timestamp) IS NULL OR event_date <= CAST(:rangeEnd AS timestamp))
        ORDER BY id ASC
        LIMIT :size
        OFFSET :from
    """, nativeQuery = true)
    List<Event> findAdminEvents(Boolean hasUsers, List<Long> users,
                                Boolean hasStates, List<String> states,
                                Boolean hasCategories, List<Long> categories,
                                LocalDateTime rangeStart,LocalDateTime rangeEnd, int from, int size);

    @EntityGraph(attributePaths = {"category", "initiator"})
    List<Event> findAllByIdIn(List<Long> ids);
}
