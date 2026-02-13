package ru.practicum.ewm.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    Event toEvent(NewEventDto newEventDto);

    EventFullDto toEventFullDto(Event event, Long views);

    EventShortDto toEventShortDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEvent(UpdateEventAdminRequest eventUpdateAdminRequest, @MappingTarget Event event);

}
