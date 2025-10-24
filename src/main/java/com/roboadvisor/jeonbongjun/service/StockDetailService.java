package com.roboadvisor.jeonbongjun.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.roboadvisor.jeonbongjun.dto.StockDetailResponse;
import com.roboadvisor.jeonbongjun.dto.NewsDto;
import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.util.StringUtils; // [추가]

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDetailService {

    private final StockRepository stockRepository;
    private final AlphaVantageService alphaVantageService;
    private final NewsService newsService;

    // [수정] 파싱 유틸리티 (빈 문자열 처리를 위해 StringUtils.hasText 사용)
    private long parseLong(String value) {
        try {
            if (!StringUtils.hasText(value)) return 0L;
            return (long) Double.parseDouble(value);
        } catch (Exception e) {
            log.warn("parseLong 실패: value='{}'", value);
            return 0L;
        }
    }

    private double parseDouble(String value) {
        try {
            if (!StringUtils.hasText(value)) return 0.0;
            return Double.parseDouble(value.replace("%", ""));
        } catch (Exception e) {
            log.warn("parseDouble 실패: value='{}'", value);
            return 0.0;
        }
    }

    public Mono<StockDetailResponse> getStockDetail(String stockCode) {

        Stock stock = stockRepository.findByStockId(stockCode)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + stockCode));

        String apiSymbol = stock.getTickerSymbol() + ".KS";
        String stockName = stock.getStockName();

        // 2. 외부 API 병렬 호출
        Mono<JsonNode> quoteMono = alphaVantageService.getGlobalQuote(apiSymbol)
                .subscribeOn(Schedulers.boundedElastic());
        Mono<JsonNode> chartMono = alphaVantageService.getDailyChartData(apiSymbol)
                .subscribeOn(Schedulers.boundedElastic());
        Mono<JsonNode> rsiMono = alphaVantageService.getRSI(apiSymbol)
                .subscribeOn(Schedulers.boundedElastic());
        Mono<JsonNode> macdMono = alphaVantageService.getMACD(apiSymbol)
                .subscribeOn(Schedulers.boundedElastic());
        Mono<JsonNode> smaMono = alphaVantageService.getSMA(apiSymbol)
                .subscribeOn(Schedulers.boundedElastic());

        // NewsService는 이미 .onErrorReturn(emptyList)가 있으므로 안전합니다.
        Mono<List<NewsDto>> newsMono = newsService.searchNews(stockName)
                .subscribeOn(Schedulers.boundedElastic());

        // 3. 모든 Mono가 완료되면 결과 조합 (JsonNode 파싱)
        return Mono.zip(quoteMono, chartMono, rsiMono, macdMono, smaMono, newsMono)
                .map(tuple -> {
                    JsonNode quote = tuple.getT1();
                    JsonNode chartData = tuple.getT2();
                    JsonNode rsiData = tuple.getT3();
                    JsonNode macdData = tuple.getT4();
                    JsonNode smaData = tuple.getT5();
                    List<NewsDto> newsData = tuple.getT6();

                    // [로그 추가] 어떤 데이터가 오는지 확인
                    log.debug("AlphaVantage Quote: {}", quote.toString());
                    log.debug("AlphaVantage Chart: {}", chartData.toString().substring(0, Math.min(chartData.toString().length(), 100)));

                    // --- 3-1. 가격 및 OHLC 파싱 (Global Quote) ---
                    // (AlphaVantageService가 빈 노드를 반환해도 파싱 유틸리티가 0을 반환)
                    long price = parseLong(quote.path("05. price").asText());
                    double changePct = parseDouble(quote.path("10. change percent").asText());
                    long changeAmt = parseLong(quote.path("09. change").asText());

                    StockDetailResponse.OhlcDto ohlc = StockDetailResponse.OhlcDto.builder()
                            .open(parseLong(quote.path("02. open").asText()))
                            .high(parseLong(quote.path("03. high").asText()))
                            .low(parseLong(quote.path("04. low").asText()))
                            .build();

                    // --- 3-2. 차트 데이터 파싱 (Time Series Daily) ---
                    List<Double> chartPoints = new ArrayList<>();
                    // (chartData가 비어있으면 elements()는 false가 됨)
                    Iterator<JsonNode> chartIterator = chartData.elements();
                    while(chartIterator.hasNext() && chartPoints.size() < 30) {
                        chartPoints.add(parseDouble(chartIterator.next().path("4. close").asText()));
                    }
                    Collections.reverse(chartPoints);

                    // --- 3-3. 기술적 지표 파싱 ---
                    double rsi = parseDouble(rsiData.elements().hasNext() ? rsiData.elements().next().path("RSI").asText() : "0");
                    double macd = parseDouble(macdData.elements().hasNext() ? macdData.elements().next().path("MACD").asText() : "0");
                    double ma20 = parseDouble(smaData.elements().hasNext() ? smaData.elements().next().path("SMA").asText() : "0");

                    StockDetailResponse.TechDto tech = StockDetailResponse.TechDto.builder()
                            .rsi(rsi)
                            .macd(macd)
                            .ma20(ma20)
                            .build();

                    // --- 3-4. 뉴스 및 리포트 파싱 ---
                    List<String> newsTitles = newsData.stream()
                            .map(NewsDto::getTitle)
                            .limit(3)
                            .collect(Collectors.toList());

                    List<StockDetailResponse.ReportDto> reports = List.of(
                            StockDetailResponse.ReportDto.builder().broker("NH투자증권").target("95,000원").stance("매수").build(),
                            StockDetailResponse.ReportDto.builder().broker("한국투자증권").target("90,000원").stance("매수").build()
                    );

                    // --- 4. 최종 DTO 조립 ---
                    return StockDetailResponse.builder()
                            .name(stock.getStockName())
                            .ticker(stock.getStockId())
                            .foreignTicker(stock.getTickerSymbol())
                            .price(price)
                            .changePct(changePct)
                            .changeAmt(changeAmt)
                            .ohlc(ohlc)
                            .tech(tech)
                            .chart(chartPoints)
                            .news(newsTitles)
                            .reports(reports)
                            .build();
                })
                .doOnError(e -> log.error("StockDetailService 최종 .map 블록 에러: {}", e.getMessage(), e)); // [수정] 스택 트레이스 포함
    }
}