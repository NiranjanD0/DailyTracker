package com.niranjan0.dailytracker;

import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final PlayerDataManager pdm;

    public LeaderboardManager(PlayerDataManager pdm) {
        this.pdm = pdm;
    }

    public List<Map.Entry<UUID, Long>> getTopDaily(int topN) {
        return getTop(topN, LeaderboardType.DAILY);
    }

    public List<Map.Entry<UUID, Long>> getTopWeekly(int topN) {
        return getTop(topN, LeaderboardType.WEEKLY);
    }

    public List<Map.Entry<UUID, Long>> getTopMonthly(int topN) {
        return getTop(topN, LeaderboardType.MONTHLY);
    }

    private List<Map.Entry<UUID, Long>> getTop(int topN, LeaderboardType type) {
        Map<UUID, Long> map = new HashMap<>();
        pdm.getPlayerDataMap().forEach((uuid, data) -> {
            switch (type) {
                case DAILY -> map.put(uuid, data.getDailyUptime());
                case WEEKLY -> map.put(uuid, data.getWeeklyUptime());
                case MONTHLY -> map.put(uuid, data.getMonthlyUptime());
            }
        });

        return map.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    private enum LeaderboardType { DAILY, WEEKLY, MONTHLY }
}