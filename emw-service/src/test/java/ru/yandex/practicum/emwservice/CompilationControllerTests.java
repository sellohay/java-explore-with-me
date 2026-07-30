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
import ru.yandex.practicum.emwservice.dtos.compilation.CompilationDto;
import ru.yandex.practicum.emwservice.dtos.compilation.NewCompilationDto;
import ru.yandex.practicum.emwservice.dtos.compilation.UpdateCompilationRequest;
import ru.yandex.practicum.emwservice.dtos.event.EventFullDto;
import ru.yandex.practicum.emwservice.dtos.event.NewEventDto;
import ru.yandex.practicum.emwservice.dtos.user.NewUserRequest;
import ru.yandex.practicum.emwservice.dtos.user.UserDto;
import ru.yandex.practicum.emwservice.model.util.Location;
import ru.yandex.practicum.emwservice.service.interfaces.CategoryService;
import ru.yandex.practicum.emwservice.service.interfaces.CompilationService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class CompilationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompilationService compilationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @MockitoBean
    private StatsClient statsClient;

    private EventFullDto event1;
    private EventFullDto event2;
    private NewCompilationDto validCompilationRequest;

    @BeforeEach
    void setUp() {
        when(statsClient.getStats(any(), any(), any(), anyBoolean()))
                .thenReturn(ResponseEntity.ok(List.of()));

        NewUserRequest userRequest = new NewUserRequest();
        userRequest.setName("CompInitiator");
        userRequest.setEmail("comp-" + UUID.randomUUID() + "@yandex.ru");
        UserDto initiator = userService.createUser(userRequest);

        NewCategoryDto catRequest = new NewCategoryDto();
        catRequest.setName("Category-" + UUID.randomUUID());
        CategoryDto category = categoryService.createCategory(catRequest);

        NewEventDto eventReq1 = createEventDto(category.getId());
        eventReq1.setTitle("First Compilation Event");
        event1 = eventService.createEvent(initiator.getId(), eventReq1);

        NewEventDto eventReq2 = createEventDto(category.getId());
        eventReq2.setTitle("Second Compilation Event");
        event2 = eventService.createEvent(initiator.getId(), eventReq2);

        validCompilationRequest = new NewCompilationDto();
        validCompilationRequest.setTitle("Best Events 2026");
        validCompilationRequest.setPinned(true);
        validCompilationRequest.setEventIds(List.of(event1.getId(), event2.getId()));
    }

    private NewEventDto createEventDto(Long catId) {
        NewEventDto dto = new NewEventDto();
        dto.setAnnotation("Annotation for event");
        dto.setDescription("Description for event");
        dto.setEventDate(LocalDateTime.now().plusDays(3));
        dto.setCategoryId(catId);
        dto.setParticipantLimit(10);
        dto.setPaid(false);
        dto.setRequestModeration(true);
        Location location = new Location(52.42, 67.67);
        dto.setLocation(location);
        return dto;
    }

    @Test
    void createCompilation_ShouldReturnCompilationDto() throws Exception {
        mockMvc.perform(post("/admin/compilations")
                        .content(objectMapper.writeValueAsString(validCompilationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title", is("Best Events 2026")))
                .andExpect(jsonPath("$.pinned", is(true)))
                .andExpect(jsonPath("$.events", hasSize(2)));
    }

    @Test
    void createCompilation_empty() throws Exception {
        validCompilationRequest.setEventIds(List.of());
        mockMvc.perform(post("/admin/compilations")
                        .content(objectMapper.writeValueAsString(validCompilationRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.events", hasSize(0)));
    }

    @Test
    void updateCompilation_updateTitleAndEvents() throws Exception {
        CompilationDto createdComp = compilationService.createCompilation(validCompilationRequest);

        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setPinned(false);
        updateRequest.setEventIds(List.of(event1.getId()));

        mockMvc.perform(patch("/admin/compilations/{compId}", createdComp.getId())
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.pinned", is(false)))
                .andExpect(jsonPath("$.events", hasSize(1)))
                .andExpect(jsonPath("$.events[0].id", is(event1.getId().intValue())));
    }

    @Test
    void deleteCompilation() throws Exception {
        CompilationDto createdComp = compilationService.createCompilation(validCompilationRequest);
        mockMvc.perform(delete("/admin/compilations/{compId}", createdComp.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/compilations/{compId}", createdComp.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCompilations_ShouldReturnFilteredList() throws Exception {
        compilationService.createCompilation(validCompilationRequest);

        NewCompilationDto unpinnedRequest = new NewCompilationDto();
        unpinnedRequest.setTitle("Unpinned events");
        unpinnedRequest.setPinned(false);
        unpinnedRequest.setEventIds(List.of(event1.getId()));
        compilationService.createCompilation(unpinnedRequest);

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Best Events 2026")));

        mockMvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getCompilationById_ShouldReturnCompilationDto() throws Exception {
        CompilationDto createdComp = compilationService.createCompilation(validCompilationRequest);

        mockMvc.perform(get("/compilations/{compId}", createdComp.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdComp.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Best Events 2026")))
                .andExpect(jsonPath("$.events", hasSize(2)));
    }

}
