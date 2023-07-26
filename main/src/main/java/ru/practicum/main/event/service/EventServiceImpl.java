package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.enumeration.EventSort;
import ru.practicum.main.enumeration.ParticipationRequestStatus;
import ru.practicum.main.enumeration.StateAction;
import ru.practicum.main.event.dto.*;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ObjectConflictException;
import ru.practicum.main.exception.ObjectNotExistException;
import ru.practicum.main.exception.ObjectValidationException;
import ru.practicum.main.location.repository.LocationRepository;
import ru.practicum.main.user.model.User;
import ru.practicum.main.user.repository.UserRepository;
import ru.practicum.statisticclient.StatisticClient;
import ru.practicum.statisticdto.HitDto;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StatisticClient statisticClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT = 2;
    private static final int MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT_ADMIN_UPDATE = 1;

    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        LocalDateTime nowTime = LocalDateTime.now();
        long duration = Duration.between(nowTime, newEventDto.getEventDate()).toHours();
        if (duration < MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT) {
            throw new ObjectValidationException(
                    String.format("Дата события должна быть не менее чем за %d часа до публикации", MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT));
        }
        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(
                () -> new ObjectValidationException(String.format("Категория с id = %d не найдена", newEventDto.getCategory())));
        User initiator = userRepository.findById(userId).orElseThrow(
                () -> new ObjectValidationException(String.format("Пользователь с id = %d не найден", userId)));
        locationRepository.save(newEventDto.getLocation());
        return EventMapper.eventToDto(eventRepository.save(EventMapper.eventFromDto(newEventDto, category, initiator, nowTime)));
    }

    public EventFullDto updateEventByInitiator(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        LocalDateTime nowTime = LocalDateTime.now();
        if (updateEventUserRequest.getEventDate() != null) {
            long duration = Duration.between(nowTime, updateEventUserRequest.getEventDate()).toHours();
            if (duration < MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT) {
                throw new ObjectValidationException(
                        String.format("Дата события должна быть не менее чем за %d часа до публикации", MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT));
            }
        }
        Event event = checkEvent(eventId, userId);
        if (event.getState() == ParticipationRequestStatus.PUBLISHED) {
            throw new ObjectConflictException("Вы не можете изменить уже опубликованное событие");
        }
        if (updateEventUserRequest.getStateAction() == StateAction.CANCEL_REVIEW) {
            event.setState(ParticipationRequestStatus.CANCELED);
        } else {
            event.setState(ParticipationRequestStatus.PENDING);
        }
        changeEventProperties(event, updateEventUserRequest);
        return EventMapper.eventToDto(eventRepository.save(event));
    }

    public EventFullDto getEventByInitiator(Long userId, Long eventId) {
        return EventMapper.eventToDto(checkEvent(eventId, userId));
    }

    public List<EventShort> getEventsByInitiator(Long userId, Integer from, Integer size) {
        return eventRepository.findByInitiatorId(userId, PageRequest.of(from, size))
                .stream()
                .map(EventMapper::eventToShort)
                .collect(Collectors.toList());
    }

    public List<EventFullDto> getEventsByAdmin(List<Long> users, ParticipationRequestStatus status, List<Long> categories,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        List<Event> events = eventRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (users != null) {
                predicates.add(root.get("initiator").in(users));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("state"), status));
            }
            if (categories != null) {
                predicates.add(root.join("category").get("id").in(categories));
            }
            if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable).getContent();
        return events
                .stream()
                .map(EventMapper::eventToDto)
                .collect(Collectors.toList());
    }

    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime eventTime = updateEventAdminRequest.getEventDate();
        if (eventTime != null) {
            if (eventTime.isBefore(currentTime)) {
                throw new ObjectValidationException("Дата начала события не может быть в прошлом");
            }
            long duration = Duration.between(currentTime, eventTime).toHours();
            if (duration < MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT_ADMIN_UPDATE) {
                throw new ObjectValidationException(String.format("Дата события должна быть не менее чем за %d часа до публикации",
                        MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT_ADMIN_UPDATE));
            }
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotExistException(String.format("Эвент с id = %d не был найден", eventId)));
        if (event.getState() != ParticipationRequestStatus.PENDING) {
            throw new ObjectConflictException("Опубликовать событие можно только в случае если его состояние «ожидает публикации»");
        }
        changeEventProperties(event, updateEventAdminRequest);
        event.setState(updateEventAdminRequest.getStateAction() == StateAction.PUBLISH_EVENT ?
                ParticipationRequestStatus.PUBLISHED : ParticipationRequestStatus.CANCELED);
        locationRepository.saveAndFlush(event.getLocation());
        eventRepository.saveAndFlush(event);
        return EventMapper.eventToDto(eventRepository.getReferenceById(eventId));
    }

    public EventFullDto getEvent(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, ParticipationRequestStatus.PUBLISHED);
        if (event == null) {
            throw new ObjectNotExistException(String.format("Эвент с id = %d не был найден", eventId));
        }
        int hitsBefore = getAmountOfUniqueViews(request);
        statisticClient.createHit(new HitDto(
                null,
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(formatter)));
        event.setViews(event.getViews() + 1);
        eventRepository.save(event);
        int hitsAfter = getAmountOfUniqueViews(request);
        if (hitsAfter <= hitsBefore) {
            event.setViews(event.getViews() - 1);
            eventRepository.save(event);
        }
        return EventMapper.eventToDto(event);
    }

    public List<EventShort> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                      LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Integer from, Integer size, HttpServletRequest request) {
        final LocalDateTime finalRangeStart = rangeStart != null ? rangeStart : LocalDateTime.now();
        final LocalDateTime finalRangeEnd = rangeEnd != null ? rangeEnd : finalRangeStart.plusYears(1);
        if (finalRangeStart.isAfter(finalRangeEnd)) {
            throw new ObjectValidationException("Дата начала сортировки должна быть ранее конца сортировки");
        }
        Specification<Event> spec = (root, query, cb) -> {
            Predicate[] predicates = {
                    cb.equal(root.get("state"), ParticipationRequestStatus.PUBLISHED),
                    text != null ? cb.or(
                            cb.like(cb.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("description")), "%" + text.toLowerCase() + "%")
                    ) : null,
                    categories != null ? root.join("category", JoinType.INNER).get("id").in(categories) : null,
                    paid != null ? cb.equal(root.get("paid"), paid) : null,
                    cb.between(root.get("eventDate"), finalRangeStart, finalRangeEnd),
                    onlyAvailable != null && onlyAvailable ? cb.greaterThan(root.get("participantLimit"), root.get("confirmedRequests")) : null
            };
            return cb.and(Stream.of(predicates).filter(Objects::nonNull).toArray(Predicate[]::new));
        };
        Pageable pageable = PageRequest.of(from, size, sort.descending());
        Page<Event> events = eventRepository.findAll(spec, pageable);
        statisticClient.createHit(new HitDto(
                null,
                "ewm-main-service",
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(formatter)));
        return events
                .stream()
                .map(EventMapper::eventToShort)
                .collect(Collectors.toList());
    }

    private int getAmountOfUniqueViews(HttpServletRequest request) {
        return statisticClient.getStatistic(
                        LocalDateTime.now().minusYears(100).format(formatter),
                        LocalDateTime.now().plusHours(1).format(formatter),
                        true,
                        request.getRequestURI())
                .size();
    }

    private Event checkEvent(Long eventId, Long userId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new ObjectNotExistException(String.format("Эвент с id = %d не был найден", eventId));
        }
        return event;
    }

    private void changeEventProperties(Event event, UpdateEventRequest updateEventRequest) {
        if (updateEventRequest.getAnnotation() != null) {
            event.setAnnotation(updateEventRequest.getAnnotation());
        }
        if (updateEventRequest.getCategory() != null) {
            event.setCategory(categoryRepository.findById(updateEventRequest.getCategory().longValue()).orElseThrow(
                    () -> new ObjectValidationException(
                            String.format("Категория с id = %d, не найдена", updateEventRequest.getCategory()))));
        }
        if (updateEventRequest.getDescription() != null) {
            event.setDescription(updateEventRequest.getDescription());
        }
        if (updateEventRequest.getEventDate() != null) {
            event.setEventDate(updateEventRequest.getEventDate());
        }
        if (updateEventRequest.getLocation() != null) {
            event.setLocation(updateEventRequest.getLocation());
        }
        if (updateEventRequest.getPaid() != null) {
            event.setPaid(updateEventRequest.getPaid());
        }
        if (updateEventRequest.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEventRequest.getParticipantLimit());
        }
        if (updateEventRequest.getTitle() != null) {
            event.setTitle(updateEventRequest.getTitle());
        }
    }
}