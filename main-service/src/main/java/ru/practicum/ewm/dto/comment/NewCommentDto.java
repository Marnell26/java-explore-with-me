package ru.practicum.ewm.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    @NotBlank
    @Size(min = 10, max = 1000, message = "Комментарий должен содержать от 10 до 1000 символов.")
    private String text;
}
