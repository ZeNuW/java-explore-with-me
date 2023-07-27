package ru.practicum.main.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.category.dto.CategoryDto;
import ru.practicum.main.category.dto.NewCategoryDto;
import ru.practicum.main.category.service.CategoryService;
import ru.practicum.main.compilation.dto.CompilationDto;
import ru.practicum.main.compilation.dto.NewCompilationDto;
import ru.practicum.main.compilation.dto.UpdateCompilationRequest;
import ru.practicum.main.compilation.service.CompilationService;
import ru.practicum.main.enumeration.ParticipationRequestStatus;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.UpdateEventAdminRequest;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.user.dto.NewUserRequest;
import ru.practicum.main.user.dto.UserDto;
import ru.practicum.main.user.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Validated
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto category) {
        log.info("Получен запрос admin/categories createCategory {}", category);
        return categoryService.createCategory(category);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Positive Long catId) {
        log.info("Получен запрос admin/categories deleteCategory c id = {}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto updateCategory(@PathVariable @Positive Long catId,
                                      @RequestBody @Valid NewCategoryDto categoryDto) {
        log.info("Получен запрос admin/categories/{catId} updateCategory, id = {} categoryDto = {}",
                catId, categoryDto);
        return categoryService.updateCategory(categoryDto, catId);
    }

    @GetMapping("/events")
    public List<EventFullDto> getEventsByAdmin(@RequestParam(required = false) List<Long> users,
                                               @RequestParam(required = false) ParticipationRequestStatus status,
                                               @RequestParam(required = false) List<Long> categories,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получен запрос admin/events getEventsByAdmin");
        return eventService.getEventsByAdmin(users, status, categories, rangeStart, rangeEnd, from, size);
    }

    @GetMapping("/users")
    public List<UserDto> getUsers(@RequestParam(name = "ids", required = false) List<Long> ids,
                                  @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                  @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("Получен запрос admin/users getUsers c ids = {}, from = {}, size = {}", ids, from, size);
        return userService.getUsers(ids, from, size);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEventByAdmin(@PathVariable @Positive Long eventId,
                                           @RequestBody @Validated UpdateEventAdminRequest adminRequest) {
        log.info("Получен запрос admin/events updateEventByAdmin, с id={}, adminRequest = {}",
                eventId, adminRequest);
        return eventService.updateEventByAdmin(eventId, adminRequest);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody @Valid NewUserRequest userRequest) {
        log.info("Получен запрос admin/users createUser c userRequest = {}", userRequest);
        return userService.createUser(userRequest);
    }

    @DeleteMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable @Positive Long userId) {
        log.info("Получен запрос admin/users deleteUser c id = {}", userId);
        userService.deleteUser(userId);
    }

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        log.info("Получен запрос admin/compilations createCompilation c newCompilationDto = {}", newCompilationDto);
        return compilationService.createCompilation(newCompilationDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable @Positive Long compId) {
        log.info("Получен запрос admin/compilations/{compId} deleteCompilation c id = {}", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCompilation(@PathVariable @Positive Long compId,
                                            @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        log.info("Получен запрос admin/compilations/{compId} updateCompilation c compId={}", compId);
        return compilationService.updateCompilation(compId, updateCompilationRequest);
    }
}