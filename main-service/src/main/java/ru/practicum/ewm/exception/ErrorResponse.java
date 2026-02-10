package ru.practicum.ewm.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ErrorResponse {
    private final String error;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private List<ValidationError> errors;

    public ErrorResponse(String error, List<ValidationError> errors) {
        this.error = error;
        this.errors = errors;
    }

    public ErrorResponse(String error) {
        this.error = error;
    }


}
