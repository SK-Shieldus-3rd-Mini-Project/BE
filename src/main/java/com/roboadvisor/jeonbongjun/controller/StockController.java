package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.StockDetailResponse;
import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import com.roboadvisor.jeonbongjun.service.StockDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockRepository stockRepository;
    private final StockDetailService stockDetailService; // 신규 상세 서비스 주입

    /**
     * 종목 검색 (상위 10개)
     */
    @GetMapping("/search")
    public ResponseEntity<List<Stock>> searchStocks(@RequestParam("query") String query) {
        Pageable pageable = PageRequest.of(0, 10);

        // [수정됨]
        // 레포지토리(StockRepository.java)에 정의된 메서드명으로 수정
        List<Stock> stocks = stockRepository.findByStockNameContainingIgnoreCase(query, pageable);

        return ResponseEntity.ok(stocks);
    }

    /**
     * [신규] 종목 상세 정보 API
     * Alpha Vantage, DeepSearch 등 여러 API의 데이터를 조합하여 반환합니다.
     */
    @GetMapping("/{stockCode}")
    public Mono<StockDetailResponse> getStockDetail(@PathVariable String stockCode) {
        // 참고: StockDetailService 에서는 stockCode(stockId)를 사용하여
        // stockRepository.findByStockId(stockCode) 를 호출해야 합니다.
        return stockDetailService.getStockDetail(stockCode);
    }
}