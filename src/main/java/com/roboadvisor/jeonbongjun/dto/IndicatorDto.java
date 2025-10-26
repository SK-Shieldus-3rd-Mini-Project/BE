package com.roboadvisor.jeonbongjun.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 경제지표 응답 DTO
 */
@Getter
@Builder
public class IndicatorDto {
    private BigDecimal interestRate;    // 기준금리 (%)
    private BigDecimal m2;              // M2 통화량 (조원)
    private BigDecimal exchangeRate;    // 원/달러 환율
    private BigDecimal gdp;             // GDP (조원)
    private String lastUpdated;         // 최종 업데이트 날짜
}