package ru.practicum.ewm.dto.category;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewCategoryDto {
    @Size(max = 50, message = "Имя должно быть не больше 50 символов")
    private String name;
}
