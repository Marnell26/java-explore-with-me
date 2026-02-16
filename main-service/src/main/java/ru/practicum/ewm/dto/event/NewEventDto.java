package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import ru.practicum.ewm.dto.Location.LocationDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.constant.Constants.DATE_TIME_FORMAT;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewEventDto {

    @NotBlank
    @Size(min = 3, max = 120, message = "Краткое описание должно содержать от 3 до 120 символов")
    private String title;

    @NotBlank
    @Size(min = 20, max = 2000, message = "Краткое описание должно содержать от 20 до 2000 символов")
    private String annotation;

    @NotBlank
    @Size(min = 20, max = 7000, message = "Полное описание должно содержать от 20 до 7000 символов")
    private String description;

    @NotNull
    private Long category;

    @NotNull
    @FutureOrPresent(message = "Дата события не может быть в прошлом")
    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    @NotNull
    private LocationDto location;

    @Builder.Default
    private Boolean paid = false;

    @Builder.Default
    @PositiveOrZero(message = "Лимит участников должен быть >=0")
    private Integer participantLimit = 0;

    @Builder.Default
    private Boolean requestModeration = true;

}
