package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCompilationDto {

    @Size(max = 50, message = "Имя подборки может содержать до 50 символов")
    private String title;

    private Set<Long> events;

    private Boolean pinned;

}
