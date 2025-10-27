package com.roboadvisor.jeonbongjun.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class StockDetailResponse {
    // 기본 정보
    private String name;
    private String ticker;
    private String foreignTicker;
    private long price;
    private double changePct;
    private long changeAmt;

    // OHLC (OhlcDto)
    private OhlcDto ohlc;

    // 기술적 지표 (TechDto)
    private TechDto tech;

    // 차트 데이터 (e.g., 30일간의 종가 리스트)
    private List<Double> chart;

    // DeepSearch 뉴스
    private List<String> news;

    // DeepSearch 리포트 (ReportDto)
    private List<ReportDto> reports;

    // --- 중첩 DTO 클래스 ---

    @Getter
    @Builder
    public static class OhlcDto {
        private long open;
        private long low;
        private long high;
    }

    @Getter
    @Builder
    public static class TechDto {
        private double rsi;
        private double macd;
        private double ma20; // 20일 이동평균선
    }

    @Getter
    @Builder
    public static class ReportDto {
        private String broker;
        private String target;
        private String stance; // "매수", "중립" 등
    }
}