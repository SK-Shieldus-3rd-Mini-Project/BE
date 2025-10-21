package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.PortfolioDto;
import com.roboadvisor.jeonbongjun.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    /**
     * API 명세: 보유 종목 추가 (POST)
     */
    @PostMapping
    public ResponseEntity<PortfolioDto.Response> addPortfolioItem(
            @PathVariable String userId,
            @RequestBody PortfolioDto.AddRequest request) {
        PortfolioDto.Response response = portfolioService.addStock(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API 명세: 보유 종목 조회 (GET)
     */
    @GetMapping
    public ResponseEntity<List<PortfolioDto.Response>> getPortfolio(@PathVariable String userId) {
        List<PortfolioDto.Response> portfolio = portfolioService.getPortfolio(userId);
        return ResponseEntity.ok(portfolio);
    }

    /**
     * API 명세: 보유 종목 수정 (PUT)
     */
    @PutMapping("/{stockId}")
    public ResponseEntity<PortfolioDto.Response> updatePortfolioItem(
            @PathVariable String userId,
            @PathVariable String stockId,
            @RequestBody PortfolioDto.UpdateRequest request) {
        PortfolioDto.Response updatedItem = portfolioService.updateStock(userId, stockId, request);
        return ResponseEntity.ok(updatedItem);
    }

    /**
     * API 명세: 보유 종목 삭제 (DELETE)
     */
    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> deletePortfolioItem(
            @PathVariable String userId,
            @PathVariable String stockId) {
        portfolioService.deleteStock(userId, stockId);
        return ResponseEntity.noContent().build();
    }
}