package ru.practicum.main.event.dto;

import ru.practicum.main.enumeration.StateAction;
import ru.practicum.main.location.model.Location;

import java.time.LocalDateTime;

public interface UpdateEventRequest {
    String getAnnotation();

    Integer getCategory();

    String getDescription();

    LocalDateTime getEventDate();

    Location getLocation();

    Boolean getPaid();

    Integer getParticipantLimit();

    Boolean getRequestModeration();

    StateAction getStateAction();

    String getTitle();

    void setAnnotation(final String annotation);

    void setCategory(final Integer category);

    void setDescription(final String description);

    void setEventDate(final LocalDateTime eventDate);

    void setLocation(final Location location);

    void setPaid(final Boolean paid);

    void setParticipantLimit(final Integer participantLimit);

    void setRequestModeration(final Boolean requestModeration);

    void setStateAction(final StateAction stateAction);

    void setTitle(final String title);
}