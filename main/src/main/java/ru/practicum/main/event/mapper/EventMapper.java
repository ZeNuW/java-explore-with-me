package ru.practicum.main.event.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.category.mapper.CategoryMapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.enumeration.ParticipationRequestStatus;
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

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
                ParticipationRequestStatus.PENDING,
                newEventDto.getTitle(),
                0
        );
    }

    public static EventFullDto eventToDto(Event event) {
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
                event.getViews()
        );
    }

    public static EventShort eventToShort(Event event) {
        return new EventShort(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.categoryToDto(event.getCategory()),
                event.getConfirmedRequests(),
                event.getEventDate(),
                UserMapper.userToShort(event.getInitiator()),
                event.getPaid(),
                event.getTitle(),
                event.getViews()
        );
    }

    public static List<EventShort> eventToShort(Iterable<Event> events) {
        List<EventShort> eventDtoList = new ArrayList<>();
        for (Event event : events) {
            eventDtoList.add(eventToShort(event));
        }
        return eventDtoList;
    }
}