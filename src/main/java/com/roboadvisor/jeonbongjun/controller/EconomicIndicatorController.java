package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.EconomicIndicatorDto;
import com.roboadvisor.jeonbongjun.entity.EconomicIndicator;
import com.roboadvisor.jeonbongjun.repository.EconomicIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 경제지표 API
 * AI 서버에서 호출하는 엔드포인트
 */
@Slf4j
@RestController
@RequestMapping("/api/indicators")
@RequiredArgsConstructor
public class EconomicIndicatorController {

    private final EconomicIndicatorRepository economicIndicatorRepository;

    /**
     * AI 서버용: 모든 경제지표 최신 데이터 조회
     * GET /api/indicators/latest
     *
     * 응답 예시:
     * {
     *   "기준금리": "3.5%",
     *   "M2 통화량": "3500조원",
     *   "원달러 환율": "1300원",
     *   "GDP 성장률": "2.5%",
     *   "소비자물가지수": "3.2%"
     * }
     */
    @GetMapping("/latest")
    public ResponseEntity<Map<String, String>> getLatestIndicators() {
        log.info("AI 서버에서 경제지표 조회 요청");

        List<EconomicIndicator> indicators = economicIndicatorRepository.findLatestByEachType();

        if (indicators.isEmpty()) {
            log.warn("경제지표 데이터가 DB에 없습니다. 샘플 데이터를 반환합니다.");
            // 샘플 데이터 (실제 운영에서는 제거)
            return ResponseEntity.ok(Map.of(
                    "기준금리", "3.5%",
                    "M2 통화량", "3500조원",
                    "원달러 환율", "1300원",
                    "GDP 성장률", "2.5%",
                    "소비자물가지수", "3.2%"
            ));
        }

        Map<String, String> result = EconomicIndicatorDto.toAiFormatMap(indicators);
        log.info("경제지표 {}개 반환", result.size());

        return ResponseEntity.ok(result);
    }

    /**
     * 프론트엔드용: 경제지표 상세 정보 조회
     * GET /api/indicators
     */
    @GetMapping
    public ResponseEntity<List<EconomicIndicatorDto>> getAllIndicators() {
        List<EconomicIndicator> indicators = economicIndicatorRepository.findLatestByEachType();

        List<EconomicIndicatorDto> dtos = indicators.stream()
                .map(EconomicIndicatorDto::fromEntity)
                .toList();

        return ResponseEntity.ok(dtos);
    }
}