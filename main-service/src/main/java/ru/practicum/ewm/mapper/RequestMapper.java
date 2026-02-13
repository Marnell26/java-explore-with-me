package ru.practicum.ewm.mapper;

import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.RequestStatus;
import ru.practicum.ewm.model.User;

import java.time.LocalDateTime;

@AnnotateWith(GeneratedMapper.class)
@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mappings({
            @Mapping(target = "event", source = "event.id"),
            @Mapping(target = "requester", source = "requester.id")
    })
    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "event", source = "event"),
            @Mapping(target = "requester", source = "user"),
            @Mapping(target = "status", source = "status"),
            @Mapping(target = "created", source = "now")
    })
    ParticipationRequest toParticipationRequest(Event event, User user, RequestStatus status, LocalDateTime now);

}
