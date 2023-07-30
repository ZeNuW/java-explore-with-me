package ru.practicum.statisticservice.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.statisticdto.HitDto;
import ru.practicum.statisticservice.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HitMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Hit hitFromDto(HitDto hitDto) {
        Hit hit = new Hit();
        hit.setApp(hitDto.getApp());
        hit.setIp(hitDto.getIp());
        hit.setCreated(LocalDateTime.parse(hitDto.getTimestamp(), formatter));
        hit.setUri(hitDto.getUri());
        return hit;
    }

    public static HitDto hitToDto(Hit hit) {
        return new HitDto(
                hit.getId(),
                hit.getApp(),
                hit.getUri(),
                hit.getIp(),
                hit.getCreated().format(formatter)
        );
    }
}