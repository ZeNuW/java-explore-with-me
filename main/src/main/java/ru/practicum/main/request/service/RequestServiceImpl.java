package ru.practicum.main.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.main.enumeration.ParticipationRequestStatus;
import ru.practicum.main.enumeration.RequestStatus;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.repository.EventRepository;
import ru.practicum.main.exception.ObjectConflictException;
import ru.practicum.main.exception.ObjectValidationException;
import ru.practicum.main.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.main.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.mapper.RequestMapper;
import ru.practicum.main.request.model.ParticipationRequest;
import ru.practicum.main.request.repository.RequestRepository;
import ru.practicum.main.user.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService { //ok

    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectValidationException(String.format("Эвент с id = %d не найден", eventId)));
        if (event.getState() != ParticipationRequestStatus.PUBLISHED) {
            throw new ObjectConflictException("Вы не можете участвовать в событии, которое ещё не опубликовано");
        }
        if (event.getConfirmedRequests() >= event.getParticipantLimit() && event.getParticipantLimit() != 0) {
            throw new ObjectConflictException("Вы не можете участвовать в событии так как был достигнут лимит запросов на участие");
        }
        if (Objects.equals(event.getInitiator().getId(), userId)) {
            throw new ObjectConflictException("Вы не может подавать заявку на участие в своём же событии");
        }
        boolean shouldConfirmRequest = !event.getRequestModeration() || event.getParticipantLimit() == 0;
        RequestStatus status = shouldConfirmRequest ? RequestStatus.CONFIRMED : RequestStatus.PENDING;
        ParticipationRequest participationRequest = RequestMapper.requestFromDto(userId, eventId);
        participationRequest.setStatus(status);
        if (shouldConfirmRequest) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }
        return RequestMapper.requestToDto(requestRepository.save(participationRequest));
    }

    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest participationRequest = requestRepository.findByRequesterAndId(userId, requestId);
        if (participationRequest == null) {
            throw new ObjectValidationException(String.format("Запрос с id = %d не был найден", requestId));
        }
        participationRequest.setStatus(RequestStatus.CANCELED);
        long eventId = participationRequest.getEvent();
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectValidationException(String.format("Эвент с id = %d не найден", eventId)));
        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventRepository.save(event);
        return RequestMapper.requestToDto(requestRepository.save(participationRequest));
    }

    public List<ParticipationRequestDto> getRequest(Long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new ObjectValidationException(String.format("Пользователь с id = %d не найден", userId)));
        return requestRepository.findAllByRequester(userId)
                .stream()
                .map(RequestMapper::requestToDto)
                .collect(Collectors.toList());
    }

    public List<ParticipationRequestDto> getRequestEventByUser(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new ObjectValidationException(String.format("Эвент с id = %d не найден", eventId));
        }
        return requestRepository.findAllByEvent(eventId)
                .stream()
                .map(RequestMapper::requestToDto)
                .collect(Collectors.toList());
    }

    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest statusUpdateRequest) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId);
        if (event == null) {
            throw new ObjectValidationException(String.format("Эвент с id = %d не найден", eventId));
        }
        Set<Long> requestIds = statusUpdateRequest.getRequestIds();
        List<ParticipationRequest> participationRequestList = requestRepository.findAllByEventAndIdIn(eventId, requestIds);
        EventRequestStatusUpdateResult requestStatusUpdateResult = new EventRequestStatusUpdateResult();
        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            participationRequestList.forEach(request -> {
                request.setStatus(RequestStatus.CONFIRMED);
                requestStatusUpdateResult.getConfirmedRequests().add(RequestMapper.requestToDto(request));
            });
            event.setConfirmedRequests(event.getConfirmedRequests() + participationRequestList.size());
        } else {
            if (event.getConfirmedRequests() >= event.getParticipantLimit()) {
                throw new ObjectConflictException("Максимальное количество участников достигнуо");
            }
            for (ParticipationRequest participationRequest : participationRequestList) {
                if (!participationRequest.getStatus().equals(RequestStatus.PENDING)) {
                    throw new ObjectValidationException("Изменение статуса возможно у заявок со статусом «ожидание»");
                }
                if (statusUpdateRequest.getStatus() == RequestStatus.CONFIRMED) {
                    participationRequest.setStatus(RequestStatus.CONFIRMED);
                    requestStatusUpdateResult.getConfirmedRequests().add(RequestMapper.requestToDto(participationRequest));
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                } else {
                    participationRequest.setStatus(RequestStatus.REJECTED);
                    requestStatusUpdateResult.getRejectedRequests().add(RequestMapper.requestToDto(participationRequest));
                }
            }
        }
        requestRepository.saveAll(participationRequestList);
        eventRepository.save(event);
        return requestStatusUpdateResult;
    }
}