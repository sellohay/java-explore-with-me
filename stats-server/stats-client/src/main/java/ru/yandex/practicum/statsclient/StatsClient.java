package ru.yandex.practicum.statsclient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.yandex.practicum.statsdto.dtos.NewEndpointHitDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsClient {
    private final RestTemplate restTemplate;

    public StatsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Autowired
    public StatsClient(@Value("${stats-server.url}") String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public ResponseEntity<Object> saveHit(String app, String uri, String ip, LocalDateTime timestamp) {
        NewEndpointHitDto hitDto = new NewEndpointHitDto(app, uri, ip, timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return restTemplate.postForEntity("/hit", hitDto, Object.class);
    }

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, Boolean unique) {
        String encodedStart = URLEncoder.encode(start, StandardCharsets.UTF_8);
        String encodedEnd = URLEncoder.encode(end, StandardCharsets.UTF_8);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("start", encodedStart);
        parameters.put("end", encodedEnd);
        if (uris != null && !uris.isEmpty()) {
            parameters.put("uris", String.join(",", uris));
        } else {
            parameters.put("uris", "");
        }

        parameters.put("unique", unique != null ? unique : false);
        return restTemplate.getForEntity(
                "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                Object.class,
                parameters
        );
    }

}
