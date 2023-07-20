package ru.practicum.statisticservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.statisticdto.HitDto;
import ru.practicum.statisticservice.service.StatisticService;

import javax.validation.Valid;

@RestController
@RequestMapping("/hit")
@RequiredArgsConstructor
@Slf4j
public class HitController {

    private final StatisticService statisticService;

    @PostMapping
    public ResponseEntity<String> createHit(@RequestBody @Valid HitDto hitDto) {
        log.info("Запрос к эндпоинту /hit - createHit с Hit: " + hitDto);
        statisticService.createHit(hitDto);
        return new ResponseEntity<>("Новый Hit сохранён", HttpStatus.CREATED);
    }
}