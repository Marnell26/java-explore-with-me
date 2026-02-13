package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.model.ParticipationRequest;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    ParticipationRequestDto toParticipationRequestDto(ParticipationRequest participationRequest);


}
