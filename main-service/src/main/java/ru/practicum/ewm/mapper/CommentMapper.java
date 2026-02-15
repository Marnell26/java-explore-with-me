package ru.practicum.ewm.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "text", source = "newCommentDto.text"),
            @Mapping(target = "author", source = "user"),
            @Mapping(target = "event", source = "event"),
            @Mapping(target = "createdOn", expression = "java(LocalDateTime.now())")
    })
    Comment toComment(NewCommentDto newCommentDto, User user, Event event);

    CommentDto toCommentDto(Comment comment);

}
