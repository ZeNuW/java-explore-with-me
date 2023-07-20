package ru.practicum.statisticservice.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.statisticdto.ViewStats;
import ru.practicum.statisticservice.dto.ViewStatsProjection;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ViewStatsMapper {

    public static ViewStats projectionToViewStats(ViewStatsProjection viewStatsProjection) {
        return new ViewStats(viewStatsProjection.getApp(), viewStatsProjection.getUri(), viewStatsProjection.getHits());
    }

    public static List<ViewStats> projectionToViewStats(Iterable<ViewStatsProjection> viewStatsProjection) {
        List<ViewStats> viewStatsList = new ArrayList<>();
        for (ViewStatsProjection projection : viewStatsProjection) {
            viewStatsList.add(projectionToViewStats(projection));
        }
        return viewStatsList;
    }
}