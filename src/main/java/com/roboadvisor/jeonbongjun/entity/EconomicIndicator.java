package com.roboadvisor.jeonbongjun.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 경제지표 엔티티
 * 기준금리, M2, 환율, GDP 등 경제 데이터 저장
 */
@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ECONOMIC_INDICATOR")
public class EconomicIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indicator_id")
    private Long indicatorId;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate; // 기록 날짜

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate; // 기준금리 (예: 3.50%)

    @Column(name = "m2", precision = 18, scale = 2)
    private BigDecimal m2; // M2 통화량 (조원 단위)

    @Column(name = "exchange_rate", precision = 10, scale = 2)
    private BigDecimal exchangeRate; // 원/달러 환율 (예: 1320.50)

    @Column(name = "gdp", precision = 18, scale = 2)
    private BigDecimal gdp; // GDP (조원 단위)
}