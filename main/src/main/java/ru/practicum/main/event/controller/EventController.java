package ru.practicum.main.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.enumeration.EventSort;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShort;
import ru.practicum.main.event.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@Validated
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<EventShort> getEvents(@RequestParam(required = false) String text,
                                      @RequestParam(required = false) List<Long> categories,
                                      @RequestParam(required = false) Boolean paid,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                      @RequestParam(required = false) Boolean onlyAvailable,
                                      @RequestParam(defaultValue = "EVENT_DATE") EventSort sort,
                                      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                      @RequestParam(defaultValue = "10") @Positive Integer size,
                                      HttpServletRequest request) {
        log.info("Получен запрос /events getEvents c text = {}, categories = {}, paid = {}, rangeStart = {}," +
                        "rangeEnd = {}, onlyAvailable = {}, sort = {}, from = {}, size = {}, request = {}", text, categories, paid,
                rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
        return eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEvent(@PathVariable @Positive Long eventId, HttpServletRequest request) {
        log.info("Получен запрос/events/{eventId} getEvent c Id={}, request = {}", eventId, request);
        return eventService.getEvent(eventId, request);
    }
}