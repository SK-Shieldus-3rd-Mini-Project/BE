package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.EconomicIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 경제지표 리포지토리
 */
@Repository
public interface EconomicIndicatorRepository extends JpaRepository<EconomicIndicator, Long> {

    /**
     * 가장 최근 경제지표 데이터 조회
     *
     * @return 최신 경제지표 (recordDate 기준 내림차순)
     */
    Optional<EconomicIndicator> findTopByOrderByRecordDateDesc();
}