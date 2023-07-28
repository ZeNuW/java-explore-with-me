package ru.practicum.main.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.enumeration.EventStatus;
import ru.practicum.main.event.dto.NewEventDto;
import ru.practicum.main.event.dto.EventFullDto;
import ru.practicum.main.event.dto.EventShort;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.location.mapper.LocationMapper;
import ru.practicum.main.user.mapper.UserMapper;
import ru.practicum.main.user.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class EventMapper {

    public static Event eventFromDto(NewEventDto newEventDto, Category category, User initiator, LocalDateTime created) {
        return new Event(null,
                newEventDto.getAnnotation(),
                category,
                0,
                created,
                newEventDto.getDescription(),
                newEventDto.getEventDate(),
                initiator,
                newEventDto.getLocation(),
                newEventDto.getPaid(),
                newEventDto.getParticipantLimit(),
                LocalDateTime.now(),
                newEventDto.getRequestModeration(),
                EventStatus.PENDING,
                newEventDto.getTitle()
        );
    }

    public static EventFullDto eventToDto(Event event, int views) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.categoryToDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getCreatedOn(),
                event.getDescription(),
                event.getEventDate(),
                UserMapper.userToShort(event.getInitiator()),
                LocationMapper.locationToDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn(),
                event.getRequestModeration(),
                event.getState(),
                event.getTitle(),
                views
        );
    }

    public static List<EventFullDto> eventToDto(Iterable<Event> events, Map<Long, Integer> views) {
        List<EventFullDto> eventDtoList = new ArrayList<>();
        int eventViews;
        for (Event event : events) {
            eventViews = views.getOrDefault(event.getId(), 0);
            eventDtoList.add(eventToDto(event, eventViews));
        }
        return eventDtoList;
    }

    public static EventShort eventToShort(Event event, int views) {
        return new EventShort(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.categoryToDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getEventDate(),
                UserMapper.userToShort(event.getInitiator()),
                event.getPaid(),
                event.getTitle(),
                views
        );
    }

    public static List<EventShort> eventToShort(Iterable<Event> events, Map<Long, Integer> views) {
        int eventViews;
        List<EventShort> eventDtoList = new ArrayList<>();
        for (Event event : events) {
            eventViews = views.getOrDefault(event.getId(), 0);
            eventDtoList.add(eventToShort(event, eventViews));
        }
        return eventDtoList;
    }
}