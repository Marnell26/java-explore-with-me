package ru.practicum.ewm.service.comment;

import org.springframework.data.domain.Pageable;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {

    void deleteCommentByAdmin(Long commentId);

    CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto);

    void deleteCommentByUser(Long userId, Long commentId);

    List<CommentDto> getComments(Long eventId, Pageable pageable);

}
