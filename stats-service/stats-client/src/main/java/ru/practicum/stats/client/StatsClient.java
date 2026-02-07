package ru.practicum.stats.client;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatsClient {
    private final RestClient restClient;

    @Value("${stats.server.url}")
    private String serverUrl;

    public StatsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void addHit(EndpointHitDto endpointHitDto) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/hit")
                .build()
                .toUri();
        restClient.post()
                .uri(uri)
                .body(endpointHitDto)
                .retrieve()
                .toBodilessEntity();
    }


    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        URI uri = UriComponentsBuilder
                .fromHttpUrl(serverUrl + "/stats")
                .queryParam("start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .queryParam("uris", uris.toArray())
                .queryParam("unique", unique)
                .build()
                .toUri();


        return restClient
                .get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ViewStatsDto>>() {
                });
    }

}
