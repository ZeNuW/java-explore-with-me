package ru.practicum.main.enumeration;

import org.springframework.data.domain.Sort;

public enum EventSort {

    VIEWS("views"),
    EVENT_DATE("eventDate");

    private final String sort;

    EventSort(String sort) {
        this.sort = sort;
    }

    public Sort toSort(Sort.Direction direction) {
        return Sort.by(direction, this.sort);
    }

    public Sort ascending() {
        return toSort(Sort.Direction.ASC);
    }

    public Sort descending() {
        return toSort(Sort.Direction.DESC);
    }
}