package ru.practicum.ewm.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.dto.request.RequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.RequestStatusUpdateResult;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.RequestMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RequestMapper requestMapper;

    @Override
    public List<ParticipationRequestDto> getUserRequests(Long userId) {
        checkUser(userId);
        return requestRepository.findAllByRequesterId(userId)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public ParticipationRequestDto addParticipationRequest(Long userId, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ConflictException("Нельзя отправить запрос на участие в своём событии");
        }

        if (requestRepository.findByRequesterIdAndEventId(userId, eventId).isPresent()) {
            throw new ConflictException("Заявка на участие уже создана");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Нельзя участвовать в неопубликованном событии");
        }

        var limit = event.getParticipantLimit();
        if (limit > 0 && limit <= requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED)) {
            throw new ConflictException("Достигнут лимит участников");
        }

        ParticipationRequest request = ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .created(LocalDateTime.now())
                .status(event.getRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED)
                .build();

        if (Boolean.FALSE.equals(event.getRequestModeration()) || event.getParticipantLimit() == 0) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }

        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        checkUser(userId);
        ParticipationRequest participationRequest = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        if (!Objects.equals(participationRequest.getRequester().getId(), userId)) {
            throw new ForbiddenException("Отменять можно только свой запрос");
        }

        participationRequest.setStatus(RequestStatus.CANCELED);

        return requestMapper.toParticipationRequestDto(participationRequest);
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        checkUser(userId);
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ForbiddenException("Запросы может просматривать только инициатор события");
        }
        return requestRepository.findAllByEventId(eventId)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public RequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
                                                         RequestStatusUpdateRequest requestStatusUpdateRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ForbiddenException("Статус запросов может менять только инициатор события");
        }

        if (requestStatusUpdateRequest.getRequestIds() == null || requestStatusUpdateRequest.getRequestIds().isEmpty()) {
            RequestStatusUpdateResult result = new RequestStatusUpdateResult();
            result.setConfirmedRequests(List.of());
            result.setRejectedRequests(List.of());
            return result;
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(requestStatusUpdateRequest.getRequestIds());

        if (requests.stream()
                .anyMatch(req -> !Objects.equals(req.getEvent().getId(), eventId)
                        || req.getStatus() != RequestStatus.PENDING)) {
            throw new ConflictException("Не все запросы из списка относятся к событию или не в статусе PENDING");
        }

        RequestStatusUpdateResult result = new RequestStatusUpdateResult();
        List<ParticipationRequestDto> confirmedDtos = new ArrayList<>();
        List<ParticipationRequestDto> rejectedDtos = new ArrayList<>();

        RequestStatus newStatus = requestStatusUpdateRequest.getStatus();

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        int limit = event.getParticipantLimit() == null ? 0 : event.getParticipantLimit();

        if (newStatus == RequestStatus.CONFIRMED) {

            if (limit > 0 && confirmedCount >= limit) {
                throw new ConflictException("Лимит заявок на участие в событии достигнут");
            }

            for (ParticipationRequest r : requests) {
                if (limit > 0 && confirmedCount >= limit) {
                    r.setStatus(RequestStatus.REJECTED);
                    rejectedDtos.add(requestMapper.toParticipationRequestDto(r));
                } else {
                    r.setStatus(RequestStatus.CONFIRMED);
                    confirmedCount++;
                    confirmedDtos.add(requestMapper.toParticipationRequestDto(r));
                }
            }
        } else if (newStatus == RequestStatus.REJECTED) {
            for (ParticipationRequest r : requests) {
                r.setStatus(RequestStatus.REJECTED);
                rejectedDtos.add(requestMapper.toParticipationRequestDto(r));
            }
        }

        requestRepository.saveAll(requests);
        result.setConfirmedRequests(confirmedDtos);
        result.setRejectedRequests(rejectedDtos);

        return result;
    }

    private void checkUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
