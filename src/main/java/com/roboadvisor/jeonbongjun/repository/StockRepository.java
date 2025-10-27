package com.roboadvisor.jeonbongjun.repository;// jeonbongjun/src/main/java/com/roboadvisor/jeonbongjun/repository/StockRepository.java

import com.roboadvisor.jeonbongjun.entity.Stock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    // 종목명에 검색어가 포함된 주식 목록 반환 (대소문자 무시)
    List<Stock> findByStockNameContainingIgnoreCase(String stockName, Pageable pageable);
    Optional<Stock> findByStockId(String stockId);
}