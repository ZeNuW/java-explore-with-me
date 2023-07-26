package ru.practicum.main.enumeration;

import lombok.Getter;

@Getter
public enum RequestStatus {
    PENDING("PENDING"),
    REJECTED("REJECTED"),
    CONFIRMED("CANCELED"),
    CANCELED("CANCELED");

    private final String value;

    RequestStatus(String value) {
        this.value = value;
    }
}