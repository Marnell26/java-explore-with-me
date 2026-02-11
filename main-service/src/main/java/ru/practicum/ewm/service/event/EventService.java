package ru.practicum.ewm.service.event;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.event.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventFullDto> getAdminEvents(List<Long> userIds, List<String> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest eventUpdateAdminRequest);

    List<EventShortDto> getEvents(Long userId, Pageable pageable);

    EventFullDto getEvent(Long userId, Long eventId);

    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest eventUpdateUserRequest);

}
