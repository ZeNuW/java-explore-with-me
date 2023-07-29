package ru.practicum.main.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.enumeration.EventSort;
import ru.practicum.main.event.dto.EventFullDtoWithViews;
import ru.practicum.main.event.dto.EventShortWithViews;
import ru.practicum.main.event.service.EventService;
import ru.practicum.statisticclient.StatisticClient;
import ru.practicum.statisticdto.HitDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/events")
@Slf4j
@Validated
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final StatisticClient statisticClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    public List<EventShortWithViews> getEvents(@RequestParam(required = false) String text,
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
        statisticClient.createHit(new HitDto(
                null,
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(formatter)));
        return eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{eventId}")
    public EventFullDtoWithViews getEvent(@PathVariable @Positive Long eventId, HttpServletRequest request) {
        log.info("Получен запрос/events/{eventId} getEvent c Id={}, request = {}", eventId, request);
        statisticClient.createHit(new HitDto(
                null,
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(formatter)));
        return eventService.getEvent(eventId);
    }
}