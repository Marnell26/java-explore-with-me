package ru.practicum.stats.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
public class StatsController {
    private final StatsService statsService;
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void addHit(@RequestBody @Valid EndpointHitDto endpointHitDto) {
        statsService.addHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public List<ViewStatsDto> getStats(
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT)
            LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT)
            LocalDateTime end,
            @RequestParam(required = false)
            List<String> uris,
            @RequestParam(required = false, defaultValue = "false")
            Boolean unique
    ) {
        return statsService.getStats(start, end, uris, unique);
    }

}
