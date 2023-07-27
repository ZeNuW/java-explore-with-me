package ru.practicum.main.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ObjectConflictException;
import ru.practicum.main.exception.ObjectNotExistException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        return CategoryMapper.categoryToDto(
                categoryRepository.save(CategoryMapper.categoryFromSaveDto(newCategoryDto)));
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = checkCategory(catId);
        checkEvent(catId);
        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        return categoryRepository.findAll(PageRequest.of(from, size))
                .getContent()
                .stream()
                .map(CategoryMapper::categoryToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto getCategory(Long catId) {
        return CategoryMapper.categoryToDto(checkCategory(catId));
    }

    @Override
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long catId) {
        Category category = checkCategory(catId);
        category.setName(newCategoryDto.getName());
        return CategoryMapper.categoryToDto(categoryRepository.save(category));
    }

    private void checkEvent(Long catId) {
        if (eventRepository.findFirstByCategoryId(catId) != null) {
            throw new ObjectConflictException("Категория содержит событие");
        }
    }

    private Category checkCategory(Long catId) {
        return categoryRepository.findById(catId).orElseThrow(
                () -> new ObjectNotExistException(String.format("Категория с id = %d, не найдена", catId)));
    }
}