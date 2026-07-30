package ru.yandex.practicum.emwservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.emwservice.dtos.user.NewUserRequest;
import ru.yandex.practicum.emwservice.dtos.user.UserDto;
import ru.yandex.practicum.emwservice.service.interfaces.UserService;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private NewUserRequest validUserRequest1;
    private NewUserRequest validUserRequest2;

    @BeforeEach
    void setUp() {
        validUserRequest1 = new NewUserRequest();
        validUserRequest1.setName("Ivan Ivanov");
        validUserRequest1.setEmail("ivan@yandex.ru");

        validUserRequest2 = new NewUserRequest();
        validUserRequest2.setName("Petr Petrov");
        validUserRequest2.setEmail("petr@yandex.ru");
    }

    @Test
    void createUser_ShouldReturnUserDto() throws Exception {
        mockMvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(validUserRequest1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name", is(validUserRequest1.getName())))
                .andExpect(jsonPath("$.email", is(validUserRequest1.getEmail())));
    }

    @Test
    void createUser_EmailInvalid() throws Exception {
        validUserRequest1.setEmail("invalid-email");
        mockMvc.perform(post("/admin/users")
                        .content(objectMapper.writeValueAsString(validUserRequest1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_NoIdsProvided() throws Exception {
        userService.createUser(validUserRequest1);
        userService.createUser(validUserRequest2);

        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(validUserRequest1.getName())))
                .andExpect(jsonPath("$[1].name", is(validUserRequest2.getName())));
    }

    @Test
    void getUsers_IdsProvided() throws Exception {
        UserDto user1 = userService.createUser(validUserRequest1);
        userService.createUser(validUserRequest2);

        mockMvc.perform(get("/admin/users")
                        .param("ids", String.valueOf(user1.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(user1.getId().intValue())));
    }

    @Test
    void deleteUser() throws Exception {
        UserDto user = userService.createUser(validUserRequest1);

        mockMvc.perform(delete("/admin/users/{userId}", user.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/admin/users").param("ids", String.valueOf(user.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


}
