package com.scaffold.service;

import com.scaffold.dto.DashboardStats;
import com.scaffold.dto.NameCount;
import com.scaffold.mapper.AdminMapper;
import com.scaffold.mapper.SysLogMapper;
import com.scaffold.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int TREND_DAYS = 7;
    private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("MM-dd");

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final SysLogMapper sysLogMapper;

    public DashboardStats overview() {
        DashboardStats stats = new DashboardStats();
        stats.setAdminCount(adminMapper.countAll());
        stats.setUserCount(userMapper.countAll());
        stats.setLogCount(sysLogMapper.countAll());
        stats.setUserTrend(fillDailyTrend(userMapper.countDailyCreated(TREND_DAYS)));
        stats.setLogTrend(fillDailyTrend(sysLogMapper.countDailyCreated(TREND_DAYS)));
        return stats;
    }

    private List<DashboardStats.TrendPoint> fillDailyTrend(List<NameCount> raw) {
        Map<String, Long> map = new HashMap<>();
        if (raw != null) {
            for (NameCount item : raw) {
                if (item.getName() != null) {
                    map.put(item.getName(), item.getValue());
                }
            }
        }
        List<DashboardStats.TrendPoint> points = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = TREND_DAYS - 1; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String key = day.format(LABEL);
            points.add(new DashboardStats.TrendPoint(key, map.getOrDefault(key, 0L)));
        }
        return points;
    }
}
