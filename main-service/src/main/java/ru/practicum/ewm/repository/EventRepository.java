package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByInitiatorId(Long initiatorId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    List<Event> findByCategoryId(Long categoryId);

    @Query("""
            SELECT e FROM Event e
            WHERE (:useUsers = false OR e.initiator.id IN :users)
              AND (:useStates = false OR e.state IN :states)
              AND (:useCategories = false OR e.category.id IN :categories)
              AND e.eventDate >= :rangeStart
              AND e.eventDate <= :rangeEnd
            """)
    List<Event> findAdminEvents(
            @Param("users") List<Long> users,
            @Param("states") List<EventState> states,
            @Param("categories") List<Long> categories,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd,
            Pageable pageable
    );

}
