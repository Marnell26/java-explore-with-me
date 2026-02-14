package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.event.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getAdminEvents(List<Long> userIds, List<String> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest eventUpdateAdminRequest);

    List<EventShortDto> getEventsByUser(Long userId, Pageable pageable);

    EventFullDto getEventByUser(Long userId, Long eventId);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest eventUpdateUserRequest);

    List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                        LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request);

    EventFullDto getPublicEvent(Long eventId, HttpServletRequest request);
}
