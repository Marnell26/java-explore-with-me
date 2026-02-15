package ru.practicum.ewm.dto.compilation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    @NotBlank
    @Size(min = 1, max = 50, message = "Имя подборки должно содержать от 1 до 50 символов")
    private String title;

    private Set<Long> events;

    @Builder.Default
    private Boolean pinned = false;
}
