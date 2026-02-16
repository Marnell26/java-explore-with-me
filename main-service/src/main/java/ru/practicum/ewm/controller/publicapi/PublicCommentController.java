package ru.practicum.ewm.controller.publicapi;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.service.comment.CommentService;

import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/comments")
@RequiredArgsConstructor
@Validated
public class PublicCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getComments(@PathVariable Long eventId,
                                        @RequestParam(defaultValue = "0") int from,
                                        @RequestParam(defaultValue = "10") int size) {
        return commentService.getComments(eventId, PageRequest.of(from / size, size));
    }

}
