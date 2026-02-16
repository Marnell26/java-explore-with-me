package ru.practicum.ewm.controller.adminapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.service.comment.CommentService;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }
}
