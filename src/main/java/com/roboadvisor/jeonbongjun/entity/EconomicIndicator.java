package com.roboadvisor.jeonbongjun.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 경제지표 엔티티
 * 한국은행 기준금리, M2 통화량, 환율, GDP 등을 저장
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ECONOMIC_INDICATOR")
public class EconomicIndicator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indicator_id")
    private Long indicatorId;

    /**
     * 지표 종류
     * - BASE_RATE: 기준금리 (%)
     * - M2: M2 통화량 (조원)
     * - EXCHANGE_RATE: 원/달러 환율
     * - GDP: GDP 성장률 (%)
     * - CPI: 소비자물가지수 (전년대비 %)
     */
    @Column(name = "indicator_type", nullable = false, length = 50)
    private String indicatorType;

    @Column(name = "indicator_name", nullable = false, length = 100)
    private String indicatorName;

    @Column(name = "value", precision = 18, scale = 4, nullable = false)
    private BigDecimal value;

    @Column(name = "unit", length = 20)
    private String unit; // %, 조원, 원 등

    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate; // 데이터 기준일

    @Column(name = "source", length = 100)
    private String source; // 한국은행, 통계청 등

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 설명
}