package ru.practicum.statisticservice.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.statisticservice.model.Hit;
import ru.practicum.statisticservice.dto.ViewStatsProjection;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(h) AS hits " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h) DESC")
    List<ViewStatsProjection> findViewStatisticsWithUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end AND h.uri IN (:uris) " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStatsProjection> findViewStatisticsWithUrisAndIpIsUnique(LocalDateTime start, LocalDateTime end,
                                                                      List<String> uris);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(h) AS hits " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(h) DESC")
    List<ViewStatsProjection> findViewStatisticsWithoutUris(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT h.app AS app, h.uri AS uri, COUNT(DISTINCT h.ip) AS hits " +
            "FROM Hit h " +
            "WHERE h.created BETWEEN :start AND :end " +
            "GROUP BY h.app, h.uri " +
            "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStatsProjection> findViewStatisticsWithoutUrisAndIsIpUnique(LocalDateTime start, LocalDateTime end,
                                                                         Pageable pageable);
}