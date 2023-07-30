package ru.practicum.main.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.request.dto.ParticipationRequestDto;
import ru.practicum.main.request.model.ParticipationRequest;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RequestMapper {
    public static ParticipationRequest requestFromDto(Long userId, Long eventId) {
        return new ParticipationRequest(
                null,
                LocalDateTime.now(),
                eventId, userId,
                null
        );
    }

    public static ParticipationRequestDto requestToDto(ParticipationRequest request) {
        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent(),
                request.getRequester(),
                request.getStatus()
        );
    }
}