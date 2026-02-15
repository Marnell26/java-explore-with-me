package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.practicum.ewm.dto.Location.LocationDto;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.user.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.constant.Constants.DATE_TIME_FORMAT;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventFullDto {
    private Long id;

    private String title;

    private String annotation;

    private String description;

    private CategoryDto category;

    private UserShortDto initiator;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private LocationDto location;

    private boolean paid;

    private int participantLimit;

    private boolean requestModeration;

    private String state;

    private int confirmedRequests;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime createdOn;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime publishedOn;

    private Long views;
}
