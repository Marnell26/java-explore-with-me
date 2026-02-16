package ru.practicum.stats.server.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.mapper.StatsMapper;
import ru.practicum.stats.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;
    private final StatsMapper statsMapper;

    @Override
    @Transactional
    public void addHit(EndpointHitDto endpointHitDto) {
        statsRepository.save(statsMapper.toEndpointHit(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new ValidationException("Даты окончания должна быть позднее даты начала");
        }
        if (unique != null && unique) {
            return statsRepository.getUniqueStats(start, end, uris);
        }
        return statsRepository.getStats(start, end, uris);

    }
}
