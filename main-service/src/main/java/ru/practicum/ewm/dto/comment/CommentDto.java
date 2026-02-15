package ru.practicum.ewm.dto.comment;

import lombok.*;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String text;
    private UserShortDto author;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
