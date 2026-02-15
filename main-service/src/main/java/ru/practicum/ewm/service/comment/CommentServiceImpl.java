package ru.practicum.ewm.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.CommentMapper;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventState;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (event.getState() != EventState.PUBLISHED) {
            throw new ForbiddenException("Комментировать можно только опубликованные события");
        }
        Comment comment = commentMapper.toComment(newCommentDto, user, event);
        commentRepository.save(comment);

        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new ForbiddenException("Удалить можно только свой комментарий");
        }
        Duration duration = Duration.between(LocalDateTime.now(), comment.getCreatedOn());
        if (duration.toMinutes() > 60) {
            throw new ForbiddenException("Комментарий можно редактировать в течение 60 минут");
        }

        comment.setText(newCommentDto.getText());
        comment.setUpdatedOn(LocalDateTime.now());

        commentRepository.save(comment);

        return commentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (!Objects.equals(comment.getAuthor().getId(), userId)) {
            throw new ForbiddenException("Удалить можно только свой комментарий");
        }
        commentRepository.deleteById(commentId);
    }

    @Override
    public List<CommentDto> getComments(Long eventId, Pageable pageable) {
        return commentRepository.findAllByEventIdOrderByCreatedOnDesc(eventId, pageable)
                .stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }
}
