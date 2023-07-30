package ru.practicum.main.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.enumeration.EventStatus;
import ru.practicum.main.event.model.Event;

import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByInitiatorId(Long userId, Pageable pageable);

    List<Event> findAllByIdIn(Set<Long> events);

    Event findFirstByCategoryId(Long catId);

    Event findByIdAndState(Long eventId, EventStatus statusParticipation);

    Event findByIdAndInitiatorId(Long eventId, Long userId);

    Page<Event> findAll(Specification<Event> spec, Pageable pageable);
}
