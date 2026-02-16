package ru.practicum.ewm.dto.category;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private int id;
    private String name;
}
