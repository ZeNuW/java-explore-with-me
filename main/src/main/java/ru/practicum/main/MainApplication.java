package ru.practicum.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"ru.practicum.statisticclient", "ru.practicum.main"})
/*
пакет такой есть, просто в другом модуле
без @ComponentScan({"ru.practicum.statisticclient"}) получаю ошибку, хотя в сервисе есть все аннотации для автоматического
создания бина и зависимость в pom:
***************************
APPLICATION FAILED TO START
***************************

Description:
Parameter 4 of constructor in ru.practicum.main.event.service.EventServiceImpl required a bean of type 'ru.practicum.statisticclient.StatisticClient' that could not be found.

Action:
Consider defining a bean of type 'ru.practicum.statisticclient.StatisticClient' in your configuration.

Если я нашёл правильную информацию то: это происходит из-за того, что они в разных модулях и в модуле statisticclient
нет аннотации @SpringBootApplication или @ComponentScan

Добавлю сюда ещё вопрос относительно или скорее попрошу помощи :)
Я пересмотрел спецификацию для Statistic service и не придумал, как мне избавиться от циклических запросов к нему
есть ли тут возможность от них уйти?
 */
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}