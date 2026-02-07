package ru.practicum.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<EndpointHit, Long> {

    @Query("select new ru.practicum.ewm.stats.dto.ViewStatsDto(eh.app, eh.uri, count(eh)) " +
            "from EndpointHit eh " +
            "where eh.timestamp between :start and :end " +
            "group by eh.app, eh.uri " +
            "order by count(h) desc")
    List<ViewStatsDto> getStats(LocalDateTime start,
                                LocalDateTime end,
                                List<String> uris);

    @Query("select new ru.practicum.ewm.stats.dto.ViewStatsDto(eh.app, eh.uri, count(distinct eh.ip)) " +
            "from EndpointHit eh " +
            "where eh.timestamp between :start and :end " +
            "group by eh.app, eh.uri " +
            "order by count(distinct eh.ip) desc")
    List<ViewStatsDto> getUniqueStats(LocalDateTime start,
                                LocalDateTime end,
                                List<String> uris);

}
