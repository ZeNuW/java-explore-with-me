package ru.practicum.main.event.service;

import ru.practicum.main.enumeration.EventSort;
import ru.practicum.main.enumeration.EventStatus;
import ru.practicum.main.event.dto.*;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShort> getEventsByInitiator(Long userId, Integer from, Integer size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDtoWithViews getEventByInitiator(Long userId, Long eventId);

    EventFullDto updateEventByInitiator(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventFullDtoWithViews> getEventsByAdmin(List<Long> users, EventStatus status, List<Long> categories,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDtoWithViews getEvent(Long eventId);

    List<EventShortWithViews> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                               LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Integer from,
                               Integer size);
}