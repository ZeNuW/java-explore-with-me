package ru.practicum.statisticservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.statisticdto.ViewStats;
import ru.practicum.statisticservice.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT new ru.practicum.statisticdto.ViewStats(h.app, h.uri, COUNT(h)) " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h) DESC")
    List<ViewStats> findViewStatisticsWithUris(LocalDateTime start, LocalDateTime end, String[] uris);

    @Query("SELECT new ru.practicum.statisticdto.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> findViewStatisticsWithUrisAndIpIsUnique(LocalDateTime start, LocalDateTime end,
                                                            String[] uris);

    @Query("SELECT new ru.practicum.statisticdto.ViewStats(h.app, h.uri, COUNT(h)) " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h) DESC")
    List<ViewStats> findViewStatisticsWithoutUris(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT new ru.practicum.statisticdto.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> findViewStatisticsWithoutUrisAndIsIpUnique(LocalDateTime start, LocalDateTime end,
                                                               Pageable pageable);
}