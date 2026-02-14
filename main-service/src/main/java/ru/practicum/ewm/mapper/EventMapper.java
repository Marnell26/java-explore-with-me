package ru.practicum.ewm.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "category", source = "category"),
            @Mapping(target = "initiator", source = "user"),
            @Mapping(target = "state", source = "state"),
            @Mapping(target = "confirmedRequests", ignore = true),
            @Mapping(target = "createdOn", source = "createdOn"),
            @Mapping(target = "publishedOn", ignore = true),
    })
    Event toEvent(NewEventDto newEventDto, User user, Category category, EventState state, LocalDateTime createdOn);

    EventFullDto toEventFullDto(Event event, Long views);

    EventShortDto toEventShortDto(Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "category", source = "category"),
            @Mapping(target = "initiator", ignore = true),
            @Mapping(target = "state", source = "state"),
            @Mapping(target = "confirmedRequests", ignore = true),
            @Mapping(target = "createdOn", ignore = true),
            @Mapping(target = "publishedOn", ignore = true),
    })
    void updateAdminEvent(UpdateEventAdminRequest eventUpdateAdminRequest, Category category, EventState state,
            @MappingTarget Event event);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "category", source = "category"),
            @Mapping(target = "initiator", ignore = true),
            @Mapping(target = "state", source = "state"),
            @Mapping(target = "confirmedRequests", ignore = true),
            @Mapping(target = "createdOn", ignore = true),
            @Mapping(target = "publishedOn", ignore = true),
    })
    void updateUserEvent(UpdateEventUserRequest updateEventUserRequest, Category category, EventState state,
            @MappingTarget Event event);

}
