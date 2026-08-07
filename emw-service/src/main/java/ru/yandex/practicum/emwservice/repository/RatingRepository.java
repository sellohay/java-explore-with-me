package ru.yandex.practicum.emwservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.emwservice.model.Rating;
import ru.yandex.practicum.emwservice.model.util.projections.EventRatingCount;
import ru.yandex.practicum.emwservice.model.util.projections.UserRatingCount;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Rating save(Rating rating);

    Optional<Rating> findByUserIdAndEventId(Long userId, Long eventId);

    @Query("""
        SELECT r.event.id as eventId,
            SUM(CASE WHEN r.liked=true THEN 1 ELSE 0 END) as likes,
            SUM(CASE WHEN r.liked=false THEN 1 ELSE 0 END) as dislikes
        FROM Rating r
        WHERE r.event.id in (:eventIds)
        GROUP BY r.event.id
    """)
    List<EventRatingCount> getEventRatingsByIds(List<Long> eventIds);

    @Query("""
        SELECT r.event.id as eventId,
            SUM(CASE WHEN r.liked=true THEN 1 ELSE 0 END) as likes,
            SUM(CASE WHEN r.liked=false THEN 1 ELSE 0 END) as dislikes
        FROM Rating r
        WHERE r.event.id = :eventId
        GROUP BY r.event.id
    """)
    EventRatingCount getEventRatingCountById(Long eventId);

    @Query("""
        SELECT COALESCE(SUM(CASE WHEN r.liked = true THEN 1 ELSE 0-1 END), 0)
        FROM Rating r
        JOIN r.event e
        WHERE e.initiator.id = :userId
    """)
    Long getAuthorRate(Long userId);

    @Query("""
        SELECT e.initiator.id AS userId,
               SUM(CASE WHEN r.liked = true THEN 1 ELSE 0-1 END) AS rating
        FROM Rating r
        JOIN r.event e
        WHERE e.initiator.id IN :userIds
        GROUP BY e.initiator.id
    """)
    List<UserRatingCount> getAuthorsRates(List<Long> userIds);
}
