package ru.practicum.statisticservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statisticdto.HitDto;
import ru.practicum.statisticdto.ViewStats;
import ru.practicum.statisticservice.service.StatisticService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping("/stats")
    public List<ViewStats> getStatistic(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
                                        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
                                        @RequestParam(defaultValue = "false") Boolean unique,
                                        @RequestParam(required = false) List<String> uris) {
        log.info("Запрос к эндпоинту /stats - getStatistic с параметрами: start = " + start +
                ", end = " + end + ", unique = " + unique + ", uris = " + uris);
        return statisticService.getStatistic(start, end, unique, uris);
    }

    @PostMapping("/hit")
    public ResponseEntity<String> createHit(@RequestBody @Valid HitDto hitDto) {
        log.info("Запрос к эндпоинту /hit - createHit с Hit: " + hitDto);
        statisticService.createHit(hitDto);
        return new ResponseEntity<>("Новый Hit сохранён", HttpStatus.CREATED);
    }
}