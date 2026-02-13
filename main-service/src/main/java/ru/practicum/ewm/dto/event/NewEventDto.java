package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.Location;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @NotNull
    private Location location;

    @Builder.Default
    private Boolean paid = false;

    @Builder.Default
    @PositiveOrZero(message = "Лимит участников должен быть >=0")
    private Integer participantLimit = 0;

    @Builder.Default
    private Boolean requestModeration = true;

}
