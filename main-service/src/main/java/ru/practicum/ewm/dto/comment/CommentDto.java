package ru.practicum.ewm.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.constant.Constants.DATE_TIME_FORMAT;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;

    private String text;

    private UserShortDto author;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime updatedOn;
}
