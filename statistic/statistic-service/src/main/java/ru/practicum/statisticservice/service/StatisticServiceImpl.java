package ru.practicum.statisticservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.statisticdto.HitDto;
import ru.practicum.statisticdto.ViewStats;
import ru.practicum.statisticservice.exception.ObjectValidationException;
import ru.practicum.statisticservice.mapper.HitMapper;
import ru.practicum.statisticservice.repository.StatisticRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StatisticServiceImpl implements StatisticService {

    private final StatisticRepository statisticRepository;

    @Override
    public HitDto createHit(HitDto hitDto) {
        return HitMapper.hitToDto(statisticRepository.save(HitMapper.hitFromDto(hitDto)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStatistic(LocalDateTime start, LocalDateTime end, Boolean unique, String[] uris) {
        if (start.isAfter(end)) {
            throw new ObjectValidationException("Начало не может быть позже конца");
        }
        if (uris == null || uris.length == 0) {
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