package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @Value("${app.name}")
    private String appName;
    private final StatsClient statsClient;

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        List<EventState> eventStates = null;
        if (states != null) {
            eventStates = states.stream()
                    .map(EventState::valueOf)
                    .toList();
        }
        List<Event> events = eventRepository.findAdminEvents(users, eventStates, categories, rangeStart, rangeEnd,
                pageable);

        return events.stream()
                .map(event -> eventMapper.toEventFullDto(event, null))
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        Category category = null;
        if (updateEventAdminRequest.getCategory() != null) {
            category = categoryRepository.findById(updateEventAdminRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        }

        EventState state = event.getState();

        EventStateAction eventStateAction = updateEventAdminRequest.getEventStateAction();

        if (event.getEventDate().minusHours(2L).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Событие можно изменить не позднее чем за 1 час до начала");
        }

        if (eventStateAction == null) {
            throw new ValidationException("Некорректное состояние");
        }

        if (eventStateAction == EventStateAction.PUBLISH_EVENT) {
            state = EventState.PUBLISHED;
        } else if (eventStateAction == EventStateAction.REJECT_EVENT) {
            state = EventState.CANCELED;
        }

        eventMapper.updateAdminEvent(updateEventAdminRequest, category, state, event);
        return eventMapper.toEventFullDto(eventRepository.save(event), getEventView(eventId));
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Pageable pageable) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return eventRepository.findAllByInitiatorId(userId, pageable)
                .stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        return eventMapper.toEventFullDto(event, getEventView(eventId));
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        Event event = eventMapper.toEvent(newEventDto, user, category, EventState.PENDING, LocalDateTime.now());
        return eventMapper.toEventFullDto(eventRepository.save(event), null);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest eventUpdateUserRequest) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        Category category = categoryRepository.findById(eventUpdateUserRequest.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        EventState state = event.getState();

        if (state == EventState.PUBLISHED) {
            throw new ConflictException("Нельзя изменить опубликованное событие");
        }

        if (event.getEventDate().minusHours(2L).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Событие можно изменить не позднее чем за 2 часа до начала");
        }

        EventStateAction eventStateAction = eventUpdateUserRequest.getEventStateAction();

        if (eventStateAction == null) {
            throw new ValidationException("Некорректное состояние");
        }

        if (eventStateAction == EventStateAction.PUBLISH_EVENT) {
            state = EventState.PUBLISHED;
        } else if (eventStateAction == EventStateAction.REJECT_EVENT) {
            state = EventState.CANCELED;
        }

        eventMapper.updateUserEvent(eventUpdateUserRequest, category, state, event);

        return eventMapper.toEventFullDto(eventRepository.save(event), getEventView(eventId));
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               String sort, Pageable pageable, HttpServletRequest request) {

        addHit(request);

        List<Event> events = eventRepository.findEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                EventState.PUBLISHED, pageable);

        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        addHit(request);

        return eventMapper.toEventFullDto(event, getEventView(eventId));
    }

    private long getEventView(Long eventId) {
        List<String> uris = List.of("/events" + eventId);
        LocalDateTime start = LocalDateTime.parse("2026-02-09 12:00:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime end = LocalDateTime.now().plusHours(1);
        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, true);

        return stats.isEmpty() ? 0L : stats.getFirst().getHits();
    }

    private void addHit(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app(appName)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        statsClient.addHit(endpointHitDto);
    }
}
