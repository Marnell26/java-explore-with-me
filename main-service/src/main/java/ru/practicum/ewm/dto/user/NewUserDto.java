package ru.practicum.ewm.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserDto {
    @Size(min = 2, max = 250, message = "Имя должно содержать от 2 до 250 символов.")
    private String name;

    @Email(message = "Некорректный формат email")
    @Size(min = 6, max = 254, message = "Адрес эл.почты должен содержать от 6 до 254 символов.")
    private String email;
}
