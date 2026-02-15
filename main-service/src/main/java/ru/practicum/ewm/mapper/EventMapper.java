package ru.practicum.ewm.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.model.Event;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, UserMapper.class})
public interface EventMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "category", ignore = true),
            @Mapping(target = "initiator", ignore = true),
            @Mapping(target = "createdOn", ignore = true),
            @Mapping(target = "publishedOn", ignore = true),
            @Mapping(target = "state", ignore = true)
    })
    Event toEvent(NewEventDto newEventDto);


    EventFullDto toEventFullDto(Event event);

    EventShortDto toEventShortDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "category", ignore = true),
            @Mapping(target = "initiator", ignore = true),
            @Mapping(target = "createdOn", ignore = true),
            @Mapping(target = "publishedOn", ignore = true),
            @Mapping(target = "state", ignore = true)
    })
    void updateAdminEvent(UpdateEventAdminRequest eventUpdateAdminRequest, @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "category", ignore = true),
            @Mapping(target = "initiator", ignore = true),
            @Mapping(target = "createdOn", ignore = true),
            @Mapping(target = "publishedOn", ignore = true),
            @Mapping(target = "state", ignore = true)
    })
    void updateUserEvent(UpdateEventUserRequest updateEventUserRequest, @MappingTarget Event event);

}
