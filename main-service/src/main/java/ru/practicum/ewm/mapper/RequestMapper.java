package ru.practicum.ewm.mapper;

import org.mapstruct.AnnotateWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.ParticipationRequest;

@AnnotateWith(GeneratedMapper.class)
@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mappings({
            @Mapping(target = "event", expression = "java(participationRequest.getEvent().getId())"),
            @Mapping(target = "requester", expression = "java(participationRequest.getRequester().getId())"),
            @Mapping(target = "status", expression = "java(participationRequest.getStatus().name())")
    })
    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest);

}
