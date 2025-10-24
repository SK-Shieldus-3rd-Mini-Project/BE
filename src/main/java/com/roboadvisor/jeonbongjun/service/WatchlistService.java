package com.roboadvisor.jeonbongjun.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.roboadvisor.jeonbongjun.dto.WatchlistDto;
import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.entity.User;
import com.roboadvisor.jeonbongjun.entity.UserWatchlist;
import com.roboadvisor.jeonbongjun.global.exception.NotFoundException;
import com.roboadvisor.jeonbongjun.repository.StockRepository; // [추가] StockRepository 임포트
import com.roboadvisor.jeonbongjun.repository.UserRepository;
import com.roboadvisor.jeonbongjun.repository.UserWatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.util.StringUtils;
import reactor.core.scheduler.Schedulers;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final UserRepository userRepository;
    private final UserWatchlistRepository userWatchlistRepository;
    private final StockRepository stockRepository; // [수정] StockRepository 주입 추가

    @Qualifier("aiWebClient")
    private final WebClient aiWebClient;
    private final JsonNode EMPTY_NODE = JsonNodeFactory.instance.objectNode();

    public Mono<List<WatchlistDto.ItemResponse>> getWatchlist(String userId) {

        Mono<List<UserWatchlist>> watchlistItemsMono = Mono.fromCallable(() ->
                userWatchlistRepository.findByUserIdWithStock(userId)
        );

        return watchlistItemsMono.subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable) // Mono<List<T>> -> Flux<T>
                .parallel()
                .flatMap(this::fetchStockPriceAndMapToDto)
                .sequential()
                .collectList();
    }

    /**
     * [신규] AI 서버(pykrx)에서 현재가/변동률을 가져와 DTO로 변환하는 메서드
     */
    private Mono<WatchlistDto.ItemResponse> fetchStockPriceAndMapToDto(UserWatchlist item) {
        String stockId = item.getStock().getStockId();
        String stockName = item.getStock().getStockName();

        // AI 서버의 /api/stock/{stock_id} 엔드포인트 호출
        return aiWebClient.get()
                .uri("/api/stock/{stockId}", stockId)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> checkApiErrorAndParse(node, stockId, stockName))
                .defaultIfEmpty(createEmptyDto(stockId, stockName))
                .onErrorReturn(createEmptyDto(stockId, stockName));
    }

    /**
     * [신규] AI 서버 응답 파싱 및 DTO 변환
     */
    private WatchlistDto.ItemResponse checkApiErrorAndParse(JsonNode node, String stockId, String stockName) {
        if (node.has("detail")) {
            log.warn("AI 서버 가격 조회 실패 (stockId: {}): {}", stockId, node.get("detail").asText());
            return createEmptyDto(stockId, stockName);
        }

        return WatchlistDto.ItemResponse.builder()
                .stockId(stockId)
                .stockName(stockName)
                .price(parseLong(node.path("price").asText()))
                .changePct(parseDouble(node.path("changePct").asText()))
                .build();
    }

    // [신규] API 실패 시 빈 DTO 생성
    private WatchlistDto.ItemResponse createEmptyDto(String stockId, String stockName) {
        return WatchlistDto.ItemResponse.builder()
                .stockId(stockId)
                .stockName(stockName)
                .price(0L)
                .changePct(0.0)
                .build();
    }

    // [신규] 파싱 유틸리티
    private long parseLong(String value) {
        try {
            if (!StringUtils.hasText(value)) return 0L;
            return (long) Double.parseDouble(value);
        } catch (Exception e) { return 0L; }
    }
    private double parseDouble(String value) {
        try {
            if (!StringUtils.hasText(value)) return 0.0;
            return Double.parseDouble(value.replace("%", ""));
        } catch (Exception e) { return 0.0; }
    }

    @Transactional
    public void add(String userId, String stockId) {
        if (userWatchlistRepository.existsByUser_UserIdAndStock_StockId(userId, stockId)) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        Stock stock = stockRepository.findByStockId(stockId)
                .orElseThrow(() -> new NotFoundException("Stock not found: " + stockId));

        UserWatchlist wl = UserWatchlist.builder()
                .user(user)
                .stock(stock)
                .build();
        try {
            userWatchlistRepository.save(wl);
        } catch (DataIntegrityViolationException e) {
            // exists 검사를 통과했음에도 불구하고 DB Unique 제약조건 위반 예외가 발생한 경우
            // (동시성/Race Condition 문제)
            // 이미 데이터가 추가된 것이므로 500 오류 대신, 경고 로그만 남기고 정상 종료합니다.
            log.warn("Watchlist add race condition detected (user: {}, stock: {}): {}",
                    userId, stockId, e.getMessage());
        }
    }

    @Transactional
    public void remove(String userId, String stockId) {
        userWatchlistRepository.deleteByUser_UserIdAndStock_StockId(userId, stockId);
    }
}