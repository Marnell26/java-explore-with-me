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
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.LocalDateTime;
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
        return requestRepository.findByRequesterId(userId)
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

        int limit = event.getParticipantLimit();
        if (limit > 0 && limit <= requestRepository.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED)) {
            throw new ConflictException("Достигнут лимит участников");
        }
        RequestStatus status = event.isRequestModeration() ? RequestStatus.PENDING : RequestStatus.CONFIRMED;
        ParticipationRequest participationRequest = requestMapper.toParticipationRequest(event, user, status,
                LocalDateTime.now());

        return requestMapper.toParticipationRequestDto(requestRepository.save(participationRequest));
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

        if (participationRequest.getStatus() == RequestStatus.CONFIRMED) {
            Event event = participationRequest.getEvent();
            event.setConfirmedRequests(event.getConfirmedRequests() - 1);
            eventRepository.save(event);
        }

        return requestMapper.toParticipationRequestDto(requestRepository.save(participationRequest));
    }

    @Override
    public List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId) {
        checkUser(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ForbiddenException("Запросы может просматривать только инициатор события");
        }
        return requestRepository.findByEventInitiatorIdAndEventId(userId, eventId)
                .stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public RequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
            RequestStatusUpdateRequest requestStatusUpdateRequest) {
        checkUser(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (!Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ForbiddenException("Статус запроса может менять только инициатор события");
        }
        List<ParticipationRequest> requests = requestRepository.findAllById(requestStatusUpdateRequest.getRequestIds());
        RequestStatus newStatus = requestStatusUpdateRequest.getStatus();

        if (newStatus == RequestStatus.CONFIRMED
                && event.getConfirmedRequests() + requests.size() > event.getParticipantLimit()) {
            throw new ConflictException("Достигнут лимит участников");
        }

        requests.forEach(r -> r.setStatus(newStatus));
        requestRepository.saveAll(requests);

        RequestStatusUpdateResult updateResult = new RequestStatusUpdateResult();
        if (newStatus == RequestStatus.CONFIRMED) {
            event.setConfirmedRequests(event.getConfirmedRequests() + requests.size());
            eventRepository.save(event);
            updateResult.getConfirmedRequests().addAll(requests.stream()
                    .map(requestMapper::toParticipationRequestDto)
                    .toList());
        } else if (newStatus == RequestStatus.REJECTED) {
            updateResult.getRejectedRequests().addAll(requests.stream()
                    .map(requestMapper::toParticipationRequestDto)
                    .toList());
        } else {
            throw new ConflictException("Указан неверный статус");
        }

        return updateResult;
    }

    private void checkUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
