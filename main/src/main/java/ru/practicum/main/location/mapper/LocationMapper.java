package ru.practicum.main.location.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.main.location.dto.LocationDto;
import ru.practicum.main.location.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocationMapper {
    public static LocationDto locationToDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }
}
