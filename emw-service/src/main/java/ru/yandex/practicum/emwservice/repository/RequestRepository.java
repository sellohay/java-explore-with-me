package ru.yandex.practicum.emwservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.yandex.practicum.emwservice.model.Request;
import ru.yandex.practicum.emwservice.model.util.ConfirmedRequestsCount;
import ru.yandex.practicum.emwservice.model.util.RequestState;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    Request save(Request request);

    boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    Integer countByEventIdAndStatus(Long eventId, RequestState status);

    List<Request> findByRequesterId(Long requesterId);

    Optional<Request> findById(Long id);

    void deleteById(Long id);

    @Query("""
        SELECT r.event.id AS eventId, COUNT(r.id) AS count
        FROM Request r
        WHERE r.status = ru.yandex.practicum.emwservice.model.util.RequestState.CONFIRMED
        AND r.event.id IN :eventIds
        GROUP BY r.event.id
    """)
    List<ConfirmedRequestsCount> getConfReqCounts(List<Long> eventIds);

    List<Request> findRequestsByEventId(Long eventId);

    List<Request> findAllByEventIdAndStatus(Long eventId, RequestState status);
}
