package ru.yandex.practicum.emwservice.service.impl;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.emwservice.dtos.category.CategoryDto;
import ru.yandex.practicum.emwservice.dtos.category.NewCategoryDto;
import ru.yandex.practicum.emwservice.dtos.mappers.CategoryMapper;
import ru.yandex.practicum.emwservice.exception.CategoryNotEmptyException;
import ru.yandex.practicum.emwservice.exception.NotFoundException;
import ru.yandex.practicum.emwservice.model.Category;
import ru.yandex.practicum.emwservice.repository.CategoryRepository;
import ru.yandex.practicum.emwservice.repository.EventRepository;
import ru.yandex.practicum.emwservice.service.interfaces.CategoryService;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.newToCategory(newCategoryDto);
        category = categoryRepository.save(category);
        return CategoryMapper.categoryToDto(category);
    }

    @Override
    public void deleteCategory(Long catId) {
        checkCategoryExists(catId);
        if (eventRepository.existsByCategoryId(catId)) {
            throw new CategoryNotEmptyException("Category is not empty");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategory(Long catId, NewCategoryDto categoryDto) {
        checkCategoryExists(catId);
        Category category = CategoryMapper.newToCategory(categoryDto);
        category.setId(catId);
        category = categoryRepository.save(category);
        return CategoryMapper.categoryToDto(category);
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        checkCategoryExists(catId);
        Optional<Category> catOpt = categoryRepository.findById(catId);
        return CategoryMapper.categoryToDto(catOpt.get());
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        List<Category> categories = categoryRepository.findAllWithLimits(from, size);
        return CategoryMapper.categoriesToDtos(categories);
    }

    @Override
    public Category getCategoryEntity(Long id) {
        Optional<Category> catOpt = categoryRepository.findById(id);
        if (catOpt.isPresent()) {
            return catOpt.get();
        } else {
            throw new NotFoundException("Category with id=" + id + " was not found");
        }
    }

    private void checkCategoryExists(Long catId) {
        if (!categoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id=" + catId + " does not exist");
        }
    }
}
