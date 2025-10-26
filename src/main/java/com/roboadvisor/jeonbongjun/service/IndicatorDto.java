package com.roboadvisor.jeonbongjun.service;

import com.roboadvisor.jeonbongjun.dto.IndicatorDto;
import com.roboadvisor.jeonbongjun.entity.EconomicIndicator;
import com.roboadvisor.jeonbongjun.repository.EconomicIndicatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * 경제지표 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndicatorService {

    private final EconomicIndicatorRepository indicatorRepository;

    /**
     * 최신 경제지표 조회
     */
    public IndicatorDto getLatestIndicators() {
        // 가장 최근 경제지표 데이터 조회
        EconomicIndicator latest = indicatorRepository.findTopByOrderByRecordDateDesc()
                .orElseThrow(() -> new RuntimeException("경제지표 데이터가 없습니다."));

        return IndicatorDto.builder()
                .interestRate(latest.getInterestRate())
                .m2(latest.getM2())
                .exchangeRate(latest.getExchangeRate())
                .gdp(latest.getGdp())
                .lastUpdated(latest.getRecordDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .build();
    }
}