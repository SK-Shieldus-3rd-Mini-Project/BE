package com.roboadvisor.jeonbongjun.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import lombok.extern.slf4j.Slf4j; // [추가] 로그
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j // [추가]
@Service
public class AlphaVantageService {

    private final WebClient webClient;
    private final String apiKey;
    private final JsonNode EMPTY_NODE = JsonNodeFactory.instance.objectNode();

    public AlphaVantageService(WebClient.Builder webClientBuilder,
                               @Value("${alphavantage.api.key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl("https://www.alphavantage.co").build();
        this.apiKey = apiKey;
    }

    /**
     * [신규] Alpha Vantage 응답을 파싱 전 에러/호출제한을 확인하는 헬퍼
     */
    private JsonNode checkApiErrorAndGetData(JsonNode node, String dataPath) {
        if (node.has("Information")) {
            // API 호출 제한(Rate Limit)에 걸림
            log.warn("Alpha Vantage API Rate Limit Hit: {}", node.get("Information").asText());
            return EMPTY_NODE;
        }
        if (node.has("Error Message")) {
            // API 키가 유효하지 않음
            log.error("Alpha Vantage API Error (Invalid Key?): {}", node.get("Error Message").asText());
            return EMPTY_NODE;
        }
        if (node.has(dataPath)) {
            // 성공
            return node.path(dataPath);
        }
        // 예상치 못한 응답 (데이터 경로가 없음)
        log.warn("Alpha Vantage response missing expected path: '{}'. Response: {}", dataPath, node.toString().substring(0, Math.min(100, node.toString().length())));
        return EMPTY_NODE;
    }

    public Mono<JsonNode> getGlobalQuote(String symbol) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/query")
                        .queryParam("function", "GLOBAL_QUOTE")
                        .queryParam("symbol", symbol)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> checkApiErrorAndGetData(node, "Global Quote")) // [수정]
                .defaultIfEmpty(EMPTY_NODE)
                .onErrorReturn(EMPTY_NODE);
    }

    public Mono<JsonNode> getDailyChartData(String symbol) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/query")
                        .queryParam("function", "TIME_SERIES_DAILY")
                        .queryParam("symbol", symbol)
                        .queryParam("outputsize", "compact")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> checkApiErrorAndGetData(node, "Time Series (Daily)")) // [수정]
                .defaultIfEmpty(EMPTY_NODE)
                .onErrorReturn(EMPTY_NODE);
    }

    public Mono<JsonNode> getRSI(String symbol) {
        return getTechIndicator(symbol, "RSI");
    }

    public Mono<JsonNode> getMACD(String symbol) {
        return getTechIndicator(symbol, "MACD");
    }

    public Mono<JsonNode> getSMA(String symbol) {
        return getTechIndicator(symbol, "SMA");
    }

    private Mono<JsonNode> getTechIndicator(String symbol, String function) {
        String dataPath = "Technical Analysis: " + function;
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/query")
                        .queryParam("function", function)
                        .queryParam("symbol", symbol)
                        .queryParam("interval", "daily")
                        .queryParam("time_period", (function.equals("SMA") ? 20 : 10))
                        .queryParam("series_type", "close")
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(node -> checkApiErrorAndGetData(node, dataPath)) // [수정]
                .defaultIfEmpty(EMPTY_NODE)
                .onErrorReturn(EMPTY_NODE);
    }
}