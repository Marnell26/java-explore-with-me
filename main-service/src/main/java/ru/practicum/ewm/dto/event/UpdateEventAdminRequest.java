package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.ewm.dto.Location.LocationDto;
import ru.practicum.ewm.model.EventStateAction;

import java.time.LocalDateTime;

import static ru.practicum.ewm.constant.Constants.DATE_TIME_FORMAT;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {

    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3 до 120 символов.")
    private String title;

    @Size(min = 20, max = 2000, message = "Аннотация должна содержать от 20 до 2000 символов.")
    private String annotation;

    @Size(min = 20, max = 7000, message = "Описание должно содержать от 20 до 7000 символов.")
    private String description;

    private Long category;

    @JsonFormat(pattern = DATE_TIME_FORMAT)
    private LocalDateTime eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero
    private Integer participantLimit;

    private Boolean requestModeration;

    private EventStateAction stateAction;

}
