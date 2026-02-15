package ru.practicum.ewm.dto.request;

import lombok.*;
import ru.practicum.ewm.model.RequestStatus;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusUpdateRequest {

    private List<Long> requestIds;

    private RequestStatus status;

}
