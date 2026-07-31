package ru.yandex.practicum.emwservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.category.CategoryDto;
import ru.yandex.practicum.emwservice.dtos.category.NewCategoryDto;
import ru.yandex.practicum.emwservice.dtos.event.EventFullDto;
import ru.yandex.practicum.emwservice.dtos.event.NewEventDto;
import ru.yandex.practicum.emwservice.dtos.event.UpdateEventAdminRequest;
import ru.yandex.practicum.emwservice.dtos.request.ParticipationRequestDto;
import ru.yandex.practicum.emwservice.dtos.user.NewUserRequest;
import ru.yandex.practicum.emwservice.dtos.user.UserDto;
import ru.yandex.practicum.emwservice.model.util.Location;
import ru.yandex.practicum.emwservice.model.util.StateActionAdmin;
import ru.yandex.practicum.emwservice.service.interfaces.CategoryService;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;
import ru.yandex.practicum.emwservice.service.interfaces.RequestService;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;
import ru.yandex.practicum.statsclient.StatsClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class RequestControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private EventService eventService;

    @Autowired
    private RequestService requestService;

    @MockitoBean
    private StatsClient statsClient;

    private UserDto initiator;
    private UserDto requester;
    private EventFullDto publishedEvent;

    @BeforeEach
    void setUp() {
        when(statsClient.getStats(any(), any(), any(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(List.of()));

        NewUserRequest initiatorRequest = new NewUserRequest();
        initiatorRequest.setName("Initiator");
        initiatorRequest.setEmail("initiator-" + UUID.randomUUID() + "@yandex.ru");
        initiator = userService.createUser(initiatorRequest);

        NewUserRequest requesterRequest = new NewUserRequest();
        requesterRequest.setName("Requester");
        requesterRequest.setEmail("requester-" + UUID.randomUUID() + "@yandex.ru");
        requester = userService.createUser(requesterRequest);

        NewCategoryDto catRequest = new NewCategoryDto();
        catRequest.setName("Category-" + UUID.randomUUID());
        CategoryDto category = categoryService.createCategory(catRequest);

        NewEventDto validEventRequest = new NewEventDto();
        validEventRequest.setAnnotation("Short annotation for test event must be long enough");
        validEventRequest.setDescription("Full description for test event must be long enough too");
        validEventRequest.setTitle("Test Event Title");
        validEventRequest.setEventDate(LocalDateTime.now().plusDays(2));
        validEventRequest.setCategoryId(category.getId());
        validEventRequest.setParticipantLimit(10);
        validEventRequest.setPaid(false);
        validEventRequest.setRequestModeration(true);

        Location location = new Location(52.42, 67.67);
        validEventRequest.setLocation(location);
        EventFullDto pendingEvent = eventService.createEvent(initiator.getId(), validEventRequest);

        UpdateEventAdminRequest publishRequest = new UpdateEventAdminRequest();
        publishRequest.setStateAction(StateActionAdmin.PUBLISH_EVENT);
        publishedEvent = eventService.updateEventAdmin(pendingEvent.getId(), publishRequest);
    }

    @Test
    void createRequest_ShouldReturnRequestDto() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", requester.getId())
                        .param("eventId", publishedEvent.getId().toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.requester", is(requester.getId().intValue())))
                .andExpect(jsonPath("$.event", is(publishedEvent.getId().intValue())))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    void createRequest_requesterIsInitiator() throws Exception {
        mockMvc.perform(post("/users/{userId}/requests", initiator.getId())
                        .param("eventId", publishedEvent.getId().toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void getRequests_ShouldReturnListOfRequests() throws Exception {
        requestService.createRequest(requester.getId(), publishedEvent.getId());
        mockMvc.perform(get("/users/{userId}/requests", requester.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].event", is(publishedEvent.getId().intValue())));
    }

    @Test
    void cancelRequest_ShouldChangeStatusToCancelled() throws Exception {
        ParticipationRequestDto requestDto = requestService.createRequest(requester.getId(), publishedEvent.getId());

        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", requester.getId(), requestDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestDto.getId().intValue())))
                .andExpect(jsonPath("$.status", is("CANCELED")));
    }

}
