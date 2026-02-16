package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.constant.Constants.DATE_TIME_FORMAT;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventShortDto {
    private Long id;

    private String title;

    private String annotation;

    private CategoryDto category;

    private UserShortDto initiator;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private boolean paid;

    private int confirmedRequests;

    private Long views;
}
