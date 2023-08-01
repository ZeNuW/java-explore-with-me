package ru.practicum.main.event.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.category.model.Category;
import ru.practicum.main.category.repository.CategoryRepository;
import ru.practicum.main.comment.dto.CommentDto;
import ru.practicum.main.comment.mapper.CommentMapper;
import ru.practicum.main.comment.model.Comment;
import ru.practicum.main.comment.repository.CommentRepository;
import ru.practicum.main.enumeration.EventSort;
import ru.practicum.main.enumeration.EventStatus;
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
import ru.practicum.main.util.StatisticsUtil;

import javax.persistence.criteria.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CommentRepository commentRepository;
    private final StatisticsUtil statisticsUtil;
    private static final int MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT = 2;
    private static final int MINIMUM_HOURS_BEFORE_EVENT_ADMIN_UPDATE = 1;

    @Override
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

    @Override
    public EventFullDto updateEventByInitiator(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        LocalDateTime nowTime = LocalDateTime.now();
        if (updateEventUserRequest.getEventDate() != null) {
            long duration = Duration.between(nowTime, updateEventUserRequest.getEventDate()).toHours();
            if (duration < MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT) {
                throw new ObjectValidationException(
                        String.format("Дата события должна быть не менее чем за %d часа до публикации",
                                MINIMUM_HOURS_BEFORE_TO_CREATE_EVENT));
            }
        }
        Event event = checkEvent(eventId, userId);
        if (event.getState() == EventStatus.PUBLISHED) {
            throw new ObjectConflictException("Вы не можете изменить уже опубликованное событие");
        }
        if (updateEventUserRequest.getStateAction() == StateAction.CANCEL_REVIEW) {
            event.setState(EventStatus.CANCELED);
        } else {
            event.setState(EventStatus.PENDING);
        }
        changeEventProperties(event, updateEventUserRequest);
        return EventMapper.eventToDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDtoWithViews getEventByInitiator(Long userId, Long eventId) {
        Event event = checkEvent(eventId, userId);
        int views = statisticsUtil.getAmountOfViews(event.getPublishedOn(), new String[]{String.format("/events/%d", eventId)});
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        return EventMapper.eventToDtoWithViews(event, views, CommentMapper.commentToDto(comments));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShort> getEventsByInitiator(Long userId, Integer from, Integer size) {
        List<Event> events = eventRepository.findByInitiatorId(userId, PageRequest.of(from, size));
        return EventMapper.eventToShort(events);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDtoWithViews> getEventsByAdmin(List<Long> users, EventStatus status, List<Long> categories,
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
        LocalDateTime minStartTime = events.parallelStream()
                .sorted(Comparator.comparing(Event::getCreatedOn))
                .collect(Collectors.toList()).get(0).getPublishedOn();
        String[] uri = events.stream().map(event -> "/events/" + event.getId()).toArray(String[]::new);
        List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
        Map<Long, List<CommentDto>> commentsMap = groupCommentsByEventId(CommentMapper.commentToDto(commentRepository.findAllByEventIdIn(eventIds)));
        return EventMapper.eventToDtoWithViews(events, statisticsUtil.getMapOfViews(minStartTime, uri), commentsMap);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime eventTime = updateEventAdminRequest.getEventDate();
        if (eventTime != null) {
            if (eventTime.isBefore(currentTime)) {
                throw new ObjectValidationException("Дата начала события не может быть в прошлом");
            }
            long duration = Duration.between(currentTime, eventTime).toHours();
            if (duration < MINIMUM_HOURS_BEFORE_EVENT_ADMIN_UPDATE) {
                throw new ObjectValidationException(String.format("Дата события должна быть не менее чем за %d часа до публикации",
                        MINIMUM_HOURS_BEFORE_EVENT_ADMIN_UPDATE));
            }
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotExistException(String.format("Эвент с id = %d не был найден", eventId)));
        if (event.getState() != EventStatus.PENDING) {
            throw new ObjectConflictException("Опубликовать событие можно только в случае если его состояние «ожидает публикации»");
        }
        changeEventProperties(event, updateEventAdminRequest);
        event.setState(updateEventAdminRequest.getStateAction() == StateAction.PUBLISH_EVENT ?
                EventStatus.PUBLISHED : EventStatus.CANCELED);
        locationRepository.saveAndFlush(event.getLocation());
        eventRepository.saveAndFlush(event);
        return EventMapper.eventToDto(eventRepository.getReferenceById(eventId));
    }

    @Override
    public EventFullDtoWithViews getEvent(Long eventId) {
        Event event = eventRepository.findByIdAndState(eventId, EventStatus.PUBLISHED);
        if (event == null) {
            throw new ObjectNotExistException(String.format("Эвент с id = %d не был найден", eventId));
        }
        int views = statisticsUtil.getAmountOfViews(event.getPublishedOn(), new String[]{String.format("/events/%d", eventId)});
        List<Comment> comments = commentRepository.findAllByEventId(eventId);
        return EventMapper.eventToDtoWithViews(event, views, CommentMapper.commentToDto(comments));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortWithViews> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd, Boolean onlyAvailable, EventSort sort, Integer from, Integer size) {
        final LocalDateTime finalRangeStart = rangeStart != null ? rangeStart : LocalDateTime.now();
        final LocalDateTime finalRangeEnd = rangeEnd != null ? rangeEnd : finalRangeStart.plusYears(1);
        if (finalRangeStart.isAfter(finalRangeEnd)) {
            throw new ObjectValidationException("Дата начала сортировки должна быть ранее конца сортировки");
        }
        Specification<Event> spec = (root, query, cb) -> {
            Predicate[] predicates = {
                    cb.equal(root.get("state"), EventStatus.PUBLISHED),
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
        List<Event> events = eventRepository.findAll(spec, pageable).getContent();
        LocalDateTime minStartTime = getMinTimeFromEventList(events);
        String[] uri = events.stream().map(event -> "/events/" + event.getId()).toArray(String[]::new);
        return EventMapper.eventToShortWithViews(events, statisticsUtil.getMapOfViews(minStartTime, uri));
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

    private LocalDateTime getMinTimeFromEventList(List<Event> events) {
        return events.parallelStream()
                .sorted(Comparator.comparing(Event::getCreatedOn))
                .collect(Collectors.toList()).get(0).getPublishedOn();
    }

    private Map<Long, List<CommentDto>> groupCommentsByEventId(List<CommentDto> allComments) {
        Map<Long, List<CommentDto>> commentsMap = new HashMap<>();
        for (CommentDto comment : allComments) {
            long eventId = comment.getEventId();
            commentsMap.computeIfAbsent(eventId, k -> new ArrayList<>()).add(comment);
        }
        return commentsMap;
    }
}