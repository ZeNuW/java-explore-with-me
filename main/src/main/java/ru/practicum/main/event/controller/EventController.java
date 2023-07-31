package ru.practicum.main.event.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.comment.dto.CommentCreateDto;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.dto.CommentUpdateDto;
import ru.practicum.main.comment.service.CommentService;
import ru.practicum.main.enumeration.EventSort;
import ru.practicum.main.event.dto.EventFullDtoWithViews;
import ru.practicum.main.event.dto.EventShortWithViews;
import ru.practicum.main.event.service.EventService;
import ru.practicum.statisticclient.StatisticClient;
import ru.practicum.statisticdto.HitDto;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
    private final CommentService commentService;
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
        log.info("Получен запрос /events/{eventId} getEvent c eventId = {}, request = {}", eventId, request);
        statisticClient.createHit(new HitDto(
                null,
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(formatter)));
        return eventService.getEvent(eventId);
    }

    @PostMapping("/{eventId}/comments/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable @Positive Long eventId, @PathVariable @Positive Long userId,
                                    @RequestBody @Valid CommentCreateDto commentCreateDto) {
        log.info("Получен запрос /{eventId}/comments/{userId} createComment c eventId = {}, userId = {}, commentCreateDto = {}",
                eventId, userId, commentCreateDto);
        return commentService.createComment(eventId, userId, commentCreateDto);
    }

    @PatchMapping("/comments/{commentId}/{userId}")
    public CommentDto updateComment(@PathVariable @Positive Long userId, @PathVariable @Positive Long commentId,
                                    @RequestBody @Valid CommentUpdateDto commentUpdateDto) {
        log.info("Получен запрос /{eventId}/comments/{userId} createComment c userId = {}, " +
                "commentId = {} ,commentUpdateDto = {}", userId, commentId, commentUpdateDto);
        return commentService.updateComment(userId, commentId, commentUpdateDto);
    }

    @DeleteMapping("/comments/{commentId}/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable @Positive Long commentId, @PathVariable @Positive Long userId) {
        log.info("Получен запрос /{eventId}/comments/{userId} createComment c userId = {}, " +
                "commentId = {}", userId, commentId);
        commentService.deleteCommentByUser(commentId, userId);
    }
}