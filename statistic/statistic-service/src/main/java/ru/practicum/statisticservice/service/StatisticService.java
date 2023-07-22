package ru.practicum.statisticservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.statisticdto.HitDto;
import ru.practicum.statisticdto.ViewStats;
import ru.practicum.statisticservice.mapper.HitMapper;
import ru.practicum.statisticservice.repository.StatisticRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final StatisticRepository statisticRepository;

    public void createHit(HitDto hitDto) {
        statisticRepository.save(HitMapper.hitFromDto(hitDto));
    }

    public List<ViewStats> getStatistic(LocalDateTime start, LocalDateTime end, Boolean unique, List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return unique ?
                    statisticRepository.findViewStatisticsWithoutUrisAndIsIpUnique(start, end, PageRequest.of(0, 10)) :
                    statisticRepository.findViewStatisticsWithoutUris(start, end, PageRequest.of(0, 10));
        } else {
            return unique ?
                    statisticRepository.findViewStatisticsWithUrisAndIpIsUnique(start, end, uris) :
                    statisticRepository.findViewStatisticsWithUris(start, end, uris);
        }
    }
}