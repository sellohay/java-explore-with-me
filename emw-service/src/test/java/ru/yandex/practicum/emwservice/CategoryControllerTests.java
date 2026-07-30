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
import ru.yandex.practicum.emwservice.dtos.category.CategoryDto;
import ru.yandex.practicum.emwservice.dtos.category.NewCategoryDto;
import ru.yandex.practicum.emwservice.service.interfaces.CategoryService;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryService categoryService;

    private NewCategoryDto catRequest1;
    private NewCategoryDto catRequest2;

    @BeforeEach
    void setUp() {
        catRequest1 = new NewCategoryDto();
        catRequest1.setName("Concerts");

        catRequest2 = new NewCategoryDto();
        catRequest2.setName("Exhibitions");
    }

    @Test
    void createCategory_ShouldReturnDto() throws Exception {
        mockMvc.perform(post("/admin/categories")
                        .content(objectMapper.writeValueAsString(catRequest1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name", is(catRequest1.getName())));
    }

    @Test
    void updateCategory_ShouldReturnUpdatedCategory() throws Exception {
        CategoryDto savedCategory = categoryService.createCategory(catRequest1);
        NewCategoryDto updateRequest = new NewCategoryDto();
        updateRequest.setName("Festivals");
        mockMvc.perform(patch("/admin/categories/{catId}", savedCategory.getId())
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedCategory.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Festivals")));
    }

    @Test
    void deleteCategory_ShouldDelete() throws Exception {
        CategoryDto savedCategory = categoryService.createCategory(catRequest1);
        mockMvc.perform(delete("/admin/categories/{catId}", savedCategory.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/categories/{catId}", savedCategory.getId()))
                .andExpect(status().isNotFound());
    }


    @Test
    void getCategories_ShouldReturnAllCategories() throws Exception {
        categoryService.createCategory(catRequest1);
        categoryService.createCategory(catRequest2);

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    void getCategoryById_ShouldReturnCategoryDto() throws Exception {
        CategoryDto savedCategory = categoryService.createCategory(catRequest1);
        mockMvc.perform(get("/categories/{catId}", savedCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedCategory.getId().intValue())))
                .andExpect(jsonPath("$.name", is(catRequest1.getName())));
    }

    @Test
    void getCategoryById_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/categories/{catId}", 999L))
                .andExpect(status().isNotFound());
    }

}
