package ru.practicum.main.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.service.EventService;
import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final EventService eventService;
    private final RequestService requestService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable @Positive Long userId,
                                    @RequestBody @Valid NewEventDto newEventDto) {
        log.info("Получен запрос users/{userId}/events createEvent userId = {}, newEventDto = {}",
                userId, newEventDto);
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{userId}/events")
    public List<EventShort> getEventsByInitiator(@PathVariable @Positive Long userId,
                                                 @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                 @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получен запрос users/{userId}/events getEventsByInitiator userId = {}, from = {}," +
                "size = {}", userId, from, size);
        return eventService.getEventsByInitiator(userId, from, size);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDtoWithViews getEventByInitiator(@PathVariable @Positive Long userId,
                                                     @PathVariable @Positive Long eventId) {
        log.info("Получен запрос users/{userId}/events/{eventId} " +
                "getEventByInitiator userId={}, eventId={}", userId, eventId);
        return eventService.getEventByInitiator(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}")
    public EventFullDto updateEventByInitiator(@PathVariable @Positive Long userId,
                                          @PathVariable @Positive Long eventId,
                                          @RequestBody @Validated UpdateEventUserRequest updateEventUserRequest) {
        log.info("Получен запрос users/{userId}/events/{eventId} updateEventByInitiator " +
                "с userId={}, eventId={}", userId, eventId);
        return eventService.updateEventByInitiator(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{userId}/events/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestEventByUser(@PathVariable @Positive Long userId,
                                                               @PathVariable @Positive Long eventId) {
        log.info("Получен запрос users/{userId}/events/{eventId}/requests getRequestEventByUser " +
                "с userId={}, eventId={}", userId, eventId);
        return requestService.getRequestEventByUser(userId, eventId);
    }

    @PatchMapping("/{userId}/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable @Positive Long userId,
                                                              @PathVariable @Positive Long eventId,
                                                              @RequestBody @Valid EventRequestStatusUpdateRequest statusUpdateRequest) {
        log.info("Получен запрос users/{userId}/events/{eventId}/requests updateRequestStatus с userId={}, eventId={}",
                userId, eventId);
        return requestService.updateRequestStatus(userId, eventId, statusUpdateRequest);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable @Positive Long userId,
                                              @RequestParam @Positive Long eventId) {
        log.info("Получен запрос users/{userId}/requests addRequest userId={}, eventId={}", userId, eventId);
        return requestService.addRequest(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getRequest(@PathVariable @Positive Long userId) {
        log.info("Получен запрос users/{userId}/requests getRequest userId={}", userId);
        return requestService.getRequest(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable @Positive Long userId,
                                                 @PathVariable @Positive Long requestId) {
        log.info("Получен запрос users/{userId}/requests/{requestId}/cancel " +
                "cancelRequest с userId={}, requestId={}", userId, requestId);
        return requestService.cancelRequest(userId, requestId);
    }
}