package ru.practicum.ewm.service.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.ViewStatsDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.ewm.model.EventStateAction.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;

    @Value("${app.name}")
    private String appName;
    private final StatsClient statsClient;

    private static final LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);

    @Override
    public List<EventFullDto> getAdminEvents(List<Long> userIds, List<EventState> states, List<Long> categories,
                                             LocalDateTime rangeStart, LocalDateTime rangeEnd, Pageable pageable) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("Дата окончания не может быть раньше даты начала");
        }

        if (rangeStart == null) {
            rangeStart = LocalDateTime.of(2000, 1, 1, 0, 0);
        }

        List<Long> usersParam = (userIds == null || userIds.isEmpty()) ? null : userIds;
        List<EventState> statesParam = (states == null || states.isEmpty()) ? null : states;
        List<Long> categoriesParam = (categories == null || categories.isEmpty()) ? null : categories;

        List<Event> events = eventRepository.findAdminEvents(
                usersParam,
                statesParam,
                categoriesParam,
                rangeStart,
                rangeEnd,
                pageable
        );

        if (events.isEmpty()) {
            return List.of();
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        List<ParticipationRequest> confirmedRequests =
                requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);

        Map<Long, Long> confirmedByEventId = confirmedRequests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getEvent().getId(),
                        Collectors.counting()
                ));

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        LocalDateTime statsEnd = LocalDateTime.now();

        List<ViewStatsDto> stats = statsClient.getStats(start, statsEnd, uris, true);

        Map<String, Long> viewsByUri = (stats == null)
                ? Collections.emptyMap()
                : stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits
                ));

        return events.stream()
                .map(event -> {
                    EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
                    long confirmed = confirmedByEventId.getOrDefault(event.getId(), 0L);
                    eventFullDto.setConfirmedRequests((int) confirmed);
                    String uri = "/events/" + event.getId();
                    long views = viewsByUri.getOrDefault(uri, 0L);
                    eventFullDto.setViews(views);
                    return eventFullDto;
                })
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest eventUpdateAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (eventUpdateAdminRequest.getStateAction() != null) {
            if (eventUpdateAdminRequest.getStateAction().equals(PUBLISH_EVENT)) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Событие уже опубликовано и не может быть опубликовано повторно");
                }
                if (event.getState() == EventState.CANCELED) {
                    throw new ConflictException("Нельзя опубликовать отменённое событие");
                }
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Публиковать можно только события в статусе PENDING");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (eventUpdateAdminRequest.getStateAction().equals(REJECT_EVENT)) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                }
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Отклонить можно только события в статусе PENDING");
                }
                event.setState(EventState.CANCELED);
            }
        }

        eventRepository.save(event);

        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Pageable pageable) {
        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }

    @Override
    public EventFullDto getEventByUser(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит данному пользователю");
        }
        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));

        Event event = eventMapper.toEvent(newEventDto);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        eventRepository.save(event);

        return eventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByUser(Long userId, Long eventId, UpdateEventUserRequest eventUpdateUserRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!event.getInitiator().getId().equals(userId)) {
            throw new NotFoundException("Событие не принадлежит данному пользователю");
        }
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Опубликованное событие нельзя изменить");
        }
        if (eventUpdateUserRequest.getCategory() != null) {
            Category category = categoryRepository.findById(eventUpdateUserRequest.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена"));
            event.setCategory(category);
        }

        eventMapper.updateUserEvent(eventUpdateUserRequest, event);

        if (eventUpdateUserRequest.getEventStateAction() != null) {
            if (eventUpdateUserRequest.getEventStateAction().equals(SEND_TO_REVIEW)) {
                event.setState(EventState.PENDING);
            } else if (eventUpdateUserRequest.getEventStateAction().equals(CANCEL_REVIEW)) {
                event.setState(EventState.CANCELED);
            }
        }

        return eventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getPublicEvents(String text, List<Long> categories, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable,
                                               String sort, int from, int size, HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new ValidationException("Дата окончания не может быть раньше даты начала");
        }

        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }

        Pageable pageable;
        if (sort == null) {
            pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        } else if (sort.equalsIgnoreCase("VIEWS")) {
            pageable = PageRequest.of(from / size, size, Sort.by("views").ascending());
        } else if (sort.equalsIgnoreCase("EVENT_DATE")) {
            pageable = PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        } else {
            throw new ValidationException("Указан некорректный вариант сортировки");
        }

        List<Long> categoriesParam = (categories == null || categories.isEmpty()) ? null : categories;

        List<Event> events = eventRepository.findEvents(categoriesParam, paid, rangeStart, rangeEnd, pageable);

        if (events.isEmpty()) {
            return List.of();
        }

        if (text != null && !text.isBlank()) {
            String lowerText = text.toLowerCase();
            events = events.stream()
                    .filter(e ->
                            (e.getAnnotation() != null &&
                                    e.getAnnotation().toLowerCase().contains(lowerText)) ||
                                    (e.getDescription() != null &&
                                            e.getDescription().toLowerCase().contains(lowerText))
                    )
                    .toList();

            if (events.isEmpty()) {
                return List.of();
            }
        }

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        List<ParticipationRequest> confirmedRequests =
                requestRepository.findAllByEventIdInAndStatus(eventIds, RequestStatus.CONFIRMED);

        Map<Long, Long> confirmedByEventId = confirmedRequests.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getEvent().getId(),
                        Collectors.counting()
                ));

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .toList();

        LocalDateTime statsEnd = LocalDateTime.now();

        List<ViewStatsDto> stats = statsClient.getStats(start, statsEnd, uris, false);

        Map<String, Long> viewsByUri = (stats == null)
                ? Collections.emptyMap()
                : stats.stream()
                .collect(Collectors.toMap(
                        ViewStatsDto::getUri,
                        ViewStatsDto::getHits
                ));

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    long confirmed = confirmedByEventId.getOrDefault(event.getId(), 0L);

                    if (Boolean.TRUE.equals(onlyAvailable)) {
                        int limitValue = event.getParticipantLimit();

                        if (limitValue > 0 && confirmed >= limitValue) {
                            return null;
                        }
                    }

                    EventShortDto eventShortDto = eventMapper.toEventShortDto(event);
                    eventShortDto.setConfirmedRequests((int) confirmed);

                    String uri = "/events/" + event.getId();
                    Long views = viewsByUri.getOrDefault(uri, 0L);
                    eventShortDto.setViews(views);

                    return eventShortDto;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if ("VIEWS".equalsIgnoreCase(sort)) {
            result.sort(Comparator.comparing(
                    EventShortDto::getViews,
                    Comparator.nullsFirst(Long::compareTo)
            ).reversed());
        }

        return result;
    }

    @Override
    public EventFullDto getPublicEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Событие не найдено");
        }

        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);

        long confirmed = requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        eventFullDto.setConfirmedRequests((int) confirmed);

        String uri = "/events/" + eventId;
        LocalDateTime statsEnd = LocalDateTime.now();

        List<ViewStatsDto> stats = statsClient.getStats(start, statsEnd, List.of(uri), true);

        long views = 0L;
        if (stats != null && !stats.isEmpty()) {
            views = stats.getFirst().getHits();
        }
        eventFullDto.setViews(views);

        return eventFullDto;
    }
}
