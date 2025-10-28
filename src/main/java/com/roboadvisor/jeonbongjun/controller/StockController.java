package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.StockDetailResponse;
import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import com.roboadvisor.jeonbongjun.service.StockDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.roboadvisor.jeonbongjun.dto.StockRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;
    private final StockDetailService stockDetailService;

    /**
     * 종목 검색 (상위 10개)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Stock>> searchStocks(@RequestParam("query") String query) {
        Pageable pageable = PageRequest.of(0, 10);
        List<Stock> stocks = stockRepository.findByStockNameContainingIgnoreCase(query, pageable);
        return ResponseEntity.ok(stocks);
    }

    /**
     * 종목 상세 정보 API (기존)
     */
    @GetMapping("/{stockCode}")
    public Mono<StockDetailResponse> getStockDetail(@PathVariable String stockCode) {
        return stockDetailService.getStockDetail(stockCode);
    }

    // 기술 지표만 반환하는 API
    @PostMapping("/tech-indicators")
    public Mono<StockDetailResponse> getTechIndicators(@RequestBody StockRequest stockRequest) {
        // stockRequest.getSymbol()은 프론트에서 받은 순수 종목코드("000020")
        log.info("기술 지표 요청: symbol={}", stockRequest.getSymbol());

        return stockDetailService.getTechIndicators(stockRequest.getSymbol())
                .doOnError(e -> log.error("기술 지표 API 호출 에러: {}", e.getMessage(), e));
    }
}