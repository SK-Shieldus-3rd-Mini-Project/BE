package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.IndicatorDto;
import com.roboadvisor.jeonbongjun.service.IndicatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 경제지표 API 컨트롤러
 * AI 서비스에서 호출하는 엔드포인트
 */
@RestController
@RequestMapping("/api/indicators")
@RequiredArgsConstructor
public class IndicatorController {

    private final IndicatorService indicatorService;

    /**
     * 최신 경제지표 조회
     *
     * @return 경제지표 데이터 (기준금리, M2, 환율 등)
     */
    @GetMapping("/latest")
    public ResponseEntity<IndicatorDto> getLatestIndicators() {
        IndicatorDto indicators = indicatorService.getLatestIndicators();
        return ResponseEntity.ok(indicators);
    }
}