package ru.practicum.main.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.main.exception.ObjectValidationException;
import ru.practicum.statisticclient.StatisticClient;
import ru.practicum.statisticdto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class StatisticsUtil {

    private final StatisticClient statisticClient;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public int getAmountOfViews(LocalDateTime eventPublishedOn, String[] uri) {
        return statisticClient.getStatistic(
                        eventPublishedOn.format(formatter),
                        LocalDateTime.now().format(formatter),
                        true,
                        uri)
                .size();
    }

    public Map<Long, Integer> getMapOfViews(LocalDateTime eventPublishedOn, String[] uri) {
        List<ViewStats> viewStatsList = statisticClient.getStatistic(
                eventPublishedOn.format(formatter),
                LocalDateTime.now().format(formatter),
                true,
                uri);
        Map<Long, Integer> idToCountMap = new HashMap<>();
        for (ViewStats viewStats : viewStatsList) {
            String viewStatsUri = viewStats.getUri();
            Long id = extractIdFromUri(viewStatsUri);
            idToCountMap.put(id, idToCountMap.getOrDefault(id, 0) + 1);
        }
        return idToCountMap;
    }

    private Long extractIdFromUri(String uri) {
        int lastSlashIndex = uri.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex < uri.length() - 1) {
            String idString = uri.substring(lastSlashIndex + 1);
            try {
                return Long.parseLong(idString);
            } catch (ObjectValidationException e) {
                throw new ObjectValidationException("Ошибка извлечения id из uri");
            }
        }
        return -1L;
    }
}
