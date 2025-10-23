package com.roboadvisor.jeonbongjun.controller;// jeonbongjun/src/main/java/com/roboadvisor/jeonbongjun/controller/StockController.java

import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository; // 혹은 StockService

    @GetMapping("/search")
    public ResponseEntity<List<Stock>> searchStocks(@RequestParam String query) {
        // 검색 결과 상위 10개만 반환
        Pageable pageable = PageRequest.of(0, 10);
        List<Stock> stocks = stockRepository.findByStockNameContainingIgnoreCase(query, pageable);
        return ResponseEntity.ok(stocks);
    }
}