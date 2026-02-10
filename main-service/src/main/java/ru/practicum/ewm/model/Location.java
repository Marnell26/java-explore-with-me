package ru.practicum.ewm.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Location {
    private float lat;
    private float lon;
}
