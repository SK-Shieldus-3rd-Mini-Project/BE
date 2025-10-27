package com.roboadvisor.jeonbongjun.dto;

import com.roboadvisor.jeonbongjun.entity.EconomicIndicator;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class EconomicIndicatorDto {

    private String indicatorType;
    private String indicatorName;
    private BigDecimal value;
    private String unit;
    private LocalDate referenceDate;
    private String source;

    /**
     * 엔티티 → DTO 변환
     */
    public static EconomicIndicatorDto fromEntity(EconomicIndicator entity) {
        return EconomicIndicatorDto.builder()
                .indicatorType(entity.getIndicatorType())
                .indicatorName(entity.getIndicatorName())
                .value(entity.getValue())
                .unit(entity.getUnit())
                .referenceDate(entity.getReferenceDate())
                .source(entity.getSource())
                .build();
    }

    /**
     * AI 서버용 Map 변환
     * FastAPI가 기대하는 형식: {"기준금리": "3.5%", "M2": "3500조원", ...}
     */
    public static Map<String, String> toAiFormatMap(List<EconomicIndicator> indicators) {
        Map<String, String> result = new HashMap<>();

        for (EconomicIndicator indicator : indicators) {
            String displayValue = indicator.getValue() + (indicator.getUnit() != null ? indicator.getUnit() : "");
            result.put(indicator.getIndicatorName(), displayValue);
        }

        return result;
    }
}