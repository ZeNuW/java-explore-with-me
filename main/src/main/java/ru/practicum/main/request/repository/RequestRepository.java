package ru.practicum.main.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.request.model.ParticipationRequest;

import java.util.List;
import java.util.Set;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {
    List<ParticipationRequest> findAllByRequester(Long userId);

    List<ParticipationRequest> findAllByEvent(Long eventId);

    ParticipationRequest findByRequesterAndId(Long userId, Long requestId);

    List<ParticipationRequest> findAllByEventAndIdIn(Long eventId, Set<Long> requestIds);
}