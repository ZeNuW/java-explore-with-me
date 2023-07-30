package ru.practicum.statisticservice.service;

import ru.practicum.statisticdto.HitDto;
import ru.practicum.statisticdto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticService {

    HitDto createHit(HitDto hitDto);

    List<ViewStats> getStatistic(LocalDateTime start, LocalDateTime end, Boolean unique, String[] uris);
}