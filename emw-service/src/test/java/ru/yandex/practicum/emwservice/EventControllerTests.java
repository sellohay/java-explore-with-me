package ru.yandex.practicum.emwservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
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
import ru.yandex.practicum.emwservice.dtos.event.UpdateEventUserRequest;
import ru.yandex.practicum.emwservice.model.util.Location;
import ru.yandex.practicum.emwservice.dtos.user.NewUserRequest;
import ru.yandex.practicum.emwservice.dtos.user.UserDto;
import ru.yandex.practicum.emwservice.model.util.StateActionAdmin;
import ru.yandex.practicum.emwservice.service.interfaces.CategoryService;
import ru.yandex.practicum.emwservice.service.interfaces.EventService;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;
import ru.yandex.practicum.statsclient.StatsClient;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class EventControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private EventService eventService;

    @MockitoBean
    private StatsClient statsClient;

    private UserDto initiator;
    private CategoryDto category;
    private NewEventDto validEventRequest;
    private EventFullDto pendingEvent;

    @BeforeEach
    void setUp() {
        when(statsClient.getStats(any(), any(), any(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(List.of()));

        NewUserRequest userRequest = new NewUserRequest();
        userRequest.setName("AdminTestUser");
        userRequest.setEmail("admin-" + UUID.randomUUID() + "@yandex.ru");
        initiator = userService.createUser(userRequest);

        NewCategoryDto catRequest = new NewCategoryDto();
        catRequest.setName("Category-" + UUID.randomUUID());
        category = categoryService.createCategory(catRequest);

        this.validEventRequest = new NewEventDto();
        validEventRequest.setAnnotation("Short annotation for test event");
        validEventRequest.setDescription("Full description for test event");
        validEventRequest.setTitle("Admin Test Event");
        validEventRequest.setEventDate(LocalDateTime.now().plusDays(2));
        validEventRequest.setCategoryId(category.getId());
        validEventRequest.setParticipantLimit(10);
        validEventRequest.setPaid(false);
        validEventRequest.setRequestModeration(true);

        Location location = new Location(55.75, 67.67);
        validEventRequest.setLocation(location);

        pendingEvent = eventService.createEvent(initiator.getId(), validEventRequest);
    }

    @Test
    void createEvent_ShouldReturnEventFullDto() throws Exception {
        mockMvc.perform(post("/users/{userId}/events", initiator.getId())
                        .content(objectMapper.writeValueAsString(validEventRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title", is(validEventRequest.getTitle())))
                .andExpect(jsonPath("$.initiator.id", is(initiator.getId().intValue())))
                .andExpect(jsonPath("$.state", is("PENDING")));
    }

    @Test
    void createEvent_DateIsInvalid() throws Exception {
        validEventRequest.setEventDate(LocalDateTime.now().plusHours(1));
        mockMvc.perform(post("/users/{userId}/events", initiator.getId())
                        .content(objectMapper.writeValueAsString(validEventRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void getEvents_ShouldReturnEventsList() throws Exception {
        mockMvc.perform(get("/users/{userId}/events", initiator.getId())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(validEventRequest.getTitle())));
    }

    @Test
    void getEventByUser_ShouldReturnEventFullDto() throws Exception {
        EventFullDto createdEvent = eventService.createEvent(initiator.getId(), validEventRequest);
        mockMvc.perform(get("/users/{userId}/events/{eventId}", initiator.getId(), createdEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdEvent.getId().intValue())))
                .andExpect(jsonPath("$.title", is(validEventRequest.getTitle())));
    }

    @Test
    void updateEventByUser_ShouldUpdateFields() throws Exception {
        EventFullDto createdEvent = eventService.createEvent(initiator.getId(), validEventRequest);
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        updateRequest.setTitle("Updated Title");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", initiator.getId(), createdEvent.getId())
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdEvent.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    void getEventRequests_ShouldReturnEmpty() throws Exception {
        EventFullDto createdEvent = eventService.createEvent(initiator.getId(), validEventRequest);
        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", initiator.getId(), createdEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getEventsAdmin_ShouldReturnEventsList() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("users", initiator.getId().toString())
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(pendingEvent.getId().intValue())));
    }

    @Test
    void updateEventAdmin_ShouldPublishEvent() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(StateActionAdmin.PUBLISH_EVENT);
        mockMvc.perform(patch("/admin/events/{eventId}", pendingEvent.getId())
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pendingEvent.getId().intValue())))
                .andExpect(jsonPath("$.state", is("PUBLISHED")));
    }

    @Test
    void updateEventAdmin_ShouldRejectEvent() throws Exception {
        UpdateEventAdminRequest updateRequest = new UpdateEventAdminRequest();
        updateRequest.setStateAction(StateActionAdmin.REJECT_EVENT);
        mockMvc.perform(patch("/admin/events/{eventId}", pendingEvent.getId())
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pendingEvent.getId().intValue())))
                .andExpect(jsonPath("$.state", is("CANCELLED")));
    }

    @Test
    void getEventsPublic_publishedEvents() throws Exception {
        UpdateEventAdminRequest publishRequest = new UpdateEventAdminRequest();
        publishRequest.setStateAction(StateActionAdmin.PUBLISH_EVENT);
        eventService.updateEventAdmin(pendingEvent.getId(), publishRequest);

        mockMvc.perform(get("/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Admin Test Event")));
    }

    @Test
    void getEventById_WhenPublished() throws Exception {
        UpdateEventAdminRequest publishRequest = new UpdateEventAdminRequest();
        publishRequest.setStateAction(StateActionAdmin.PUBLISH_EVENT);
        eventService.updateEventAdmin(pendingEvent.getId(), publishRequest);

        mockMvc.perform(get("/events/{id}", pendingEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(pendingEvent.getId().intValue())))
                .andExpect(jsonPath("$.state", is("PUBLISHED")));
    }

    @Test
    void getEventById_WhenNotPublished() throws Exception {
        mockMvc.perform(get("/events/{id}", pendingEvent.getId()))
                .andExpect(status().isConflict());
    }
}
