package ru.practicum.statisticservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.statisticdto.ViewStats;
import ru.practicum.statisticservice.service.StatisticService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stats")
@Slf4j
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping
    public List<ViewStats> getStatistic(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                        @RequestParam(defaultValue = "false") Boolean unique,
                                        @RequestParam(required = false) List<String> uris) {
        log.info("Запрос к эндпоинту /stats - getStatistic с параметрами: start = " + start +
                ", end = " + end + ", unique = " + unique + ", uris = " + uris);
        return statisticService.getStatistic(start, end, unique, uris);
    }
}