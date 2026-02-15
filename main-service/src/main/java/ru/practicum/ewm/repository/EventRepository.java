package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    List<Event> findByCategoryId(Long categoryId);


    @Query("SELECT e FROM Event e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND e.eventDate >= COALESCE(:rangeStart, e.eventDate) " +
            "AND e.eventDate <= COALESCE(:rangeEnd, e.eventDate) " +
            "ORDER BY e.id")
    List<Event> findAdminEvents(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

    @Query("SELECT e FROM Event e " +
            "WHERE e.state = ru.practicum.ewm.model.EventState.PUBLISHED " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND e.eventDate >= :rangeStart " +
            "AND e.eventDate <= COALESCE(:rangeEnd, e.eventDate)")
    List<Event> findEvents(
            @Param("categories") List<Long> categories,
            @Param("paid") Boolean paid,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

}
