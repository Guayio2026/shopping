package com.scaffold.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardStats {

    private long adminCount;
    private long userCount;
    private long logCount;

    /** 近 7 日用户新增 */
    private List<TrendPoint> userTrend = new ArrayList<>();

    /** 近 7 日日志量 */
    private List<TrendPoint> logTrend = new ArrayList<>();

    @Data
    public static class TrendPoint {
        private String date;
        private long value;

        public TrendPoint() {
        }

        public TrendPoint(String date, long value) {
            this.date = date;
            this.value = value;
        }
    }
}
