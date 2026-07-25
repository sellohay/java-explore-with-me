package ru.yandex.practicum.statsservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.statsdto.dtos.NewEndpointHitDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest(classes = StatsServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatsControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void shouldSaveHitAndReturnCreatedStatus() throws Exception {
        NewEndpointHitDto hitDto = new NewEndpointHitDto(
                "ewm-main-service",
                "/events/1",
                "192.163.0.1",
                LocalDateTime.now().format(formatter)
        );

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hitDto)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetStatsCorrectly() throws Exception {
        String timestamp1 = LocalDateTime.now().minusHours(1).format(formatter);
        String timestamp2 = LocalDateTime.now().format(formatter);

        mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new NewEndpointHitDto("ewm-main-service", "/events/1", "192.163.0.1", timestamp1))));

        mockMvc.perform(post("/hit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new NewEndpointHitDto("ewm-main-service", "/events/1", "192.163.0.1", timestamp2))));

        String start = LocalDateTime.now().minusDays(1).format(formatter);
        String end = LocalDateTime.now().plusDays(1).format(formatter);

        mockMvc.perform(get("/stats?start=" + start + "&end=" + end + "&uris=/events/1&unique=false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].app").value("ewm-main-service"))
                .andExpect(jsonPath("$[0].uri").value("/events/1"))
                .andExpect(jsonPath("$[0].hits").value(2));

        mockMvc.perform(get("/stats?start=" + start + "&end=" + end + "&uris=/events/1&unique=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hits").value(1));
    }
}