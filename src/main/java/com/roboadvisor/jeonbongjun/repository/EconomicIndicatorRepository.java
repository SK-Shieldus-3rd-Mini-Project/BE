package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.EconomicIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EconomicIndicatorRepository extends JpaRepository<EconomicIndicator, Long> {

    /**
     * 특정 지표의 최신 데이터 조회
     */
    Optional<EconomicIndicator> findFirstByIndicatorTypeOrderByReferenceDateDesc(String indicatorType);

    /**
     * 모든 지표의 최신 데이터 조회 (AI 서버용)
     * 각 indicatorType별로 가장 최신 데이터 1건씩 반환
     */
    @Query(value = "SELECT e1.* FROM ECONOMIC_INDICATOR e1 " +
            "INNER JOIN (" +
            "    SELECT indicator_type, MAX(reference_date) as max_date " +
            "    FROM ECONOMIC_INDICATOR " +
            "    GROUP BY indicator_type" +
            ") e2 ON e1.indicator_type = e2.indicator_type " +
            "    AND e1.reference_date = e2.max_date",
            nativeQuery = true)
    List<EconomicIndicator> findLatestByEachType();
}