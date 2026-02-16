package ru.practicum.ewm.service.request;

import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.dto.request.RequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.RequestStatusUpdateResult;

import java.util.List;

public interface RequestService {

    List<ParticipationRequestDto> getUserRequests(Long userId);

    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getEventParticipants(Long userId, Long eventId);

    RequestStatusUpdateResult changeRequestStatus(Long userId, Long eventId,
            RequestStatusUpdateRequest requestStatusUpdateRequest);

}
