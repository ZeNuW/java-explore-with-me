package ru.practicum.statisticclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.statisticdto.HitDto;
import ru.practicum.statisticdto.ViewStats;

import java.util.List;
import java.util.Map;

@Service
public class StatisticClient extends BaseClient {

    private static final String API_PREFIX_HIT = "/hit";
    private static final String API_PREFIX_STATS = "/stats";

    public StatisticClient(@Value("${statistic-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public List<ViewStats> getStatistic(String start, String end, Boolean unique, String[] uris) {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "unique", unique,
                "uris", uris
        );
        String path = API_PREFIX_STATS + "?start={start}&end={end}&uris={uris}&unique={unique}";
        ResponseEntity<List<ViewStats>> serverResponse = rest.exchange(path, HttpMethod.GET,
                null, new ParameterizedTypeReference<>() {}, parameters);
        return serverResponse.getBody();
    }

    public void createHit(HitDto hitDto) {
        post(API_PREFIX_HIT, hitDto);
    }
}