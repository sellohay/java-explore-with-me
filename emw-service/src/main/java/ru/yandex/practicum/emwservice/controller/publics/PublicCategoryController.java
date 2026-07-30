package ru.yandex.practicum.emwservice.controller.publics;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.emwservice.dtos.category.CategoryDto;
import ru.yandex.practicum.emwservice.service.interfaces.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class PublicCategoryController {

    private final CategoryService categoryService;

    public PublicCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(required = false, defaultValue = "0") int from,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategoryById(@PathVariable long catId) {
        return categoryService.getCategoryById(catId);
    }
}

