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
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDetailService {

    private final StockRepository stockRepository;
    private final AlphaVantageService alphaVantageService;
    private final NewsService newsService;
    private final YahooFinanceService yahooFinanceService; // 신규 추가

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

        String apiSymbol = stockCode + ".KS";
        String stockName = stock.getStockName();

        // 외부 API 병렬 호출
        Mono<JsonNode> quoteMono = alphaVantageService.getGlobalQuote(apiSymbol)
                .subscribeOn(Schedulers.boundedElastic());

        // Yahoo Finance로 기술 지표 계산 (RSI, MACD, SMA, 차트)
        Mono<YahooFinanceService.TechnicalIndicators> techMono =
                yahooFinanceService.getTechnicalIndicators(apiSymbol)
                        .subscribeOn(Schedulers.boundedElastic());

        Mono<List<NewsDto>> newsMono = newsService.searchNews(stockName)
                .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(quoteMono, techMono, newsMono)
                .map(tuple -> {
                    JsonNode quote = tuple.getT1();
                    YahooFinanceService.TechnicalIndicators techData = tuple.getT2();
                    List<NewsDto> newsData = tuple.getT3();

                    log.debug("AlphaVantage Quote: {}", quote.toString());
                    log.debug("Yahoo Finance Tech: RSI={}, MACD={}, MA20={}",
                            techData.getRsi(), techData.getMacd(), techData.getMa20());

                    // 가격 및 OHLC 파싱 (AlphaVantage)
                    long price = parseLong(quote.path("05. price").asText());
                    double changePct = parseDouble(quote.path("10. change percent").asText());
                    long changeAmt = parseLong(quote.path("09. change").asText());

                    StockDetailResponse.OhlcDto ohlc = StockDetailResponse.OhlcDto.builder()
                            .open(parseLong(quote.path("02. open").asText()))
                            .high(parseLong(quote.path("03. high").asText()))
                            .low(parseLong(quote.path("04. low").asText()))
                            .build();

                    // 기술 지표 (Yahoo Finance + TA4J 계산)
                    StockDetailResponse.TechDto tech = StockDetailResponse.TechDto.builder()
                            .rsi(techData.getRsi())
                            .macd(techData.getMacd())
                            .ma20(techData.getMa20())
                            .build();

                    // 차트 데이터 (Yahoo Finance)
                    List<Double> chartPoints = techData.getChartData();

                    // 뉴스 파싱
                    List<String> newsTitles = newsData.stream()
                            .map(NewsDto::getTitle)
                            .limit(3)
                            .collect(Collectors.toList());

                    List<StockDetailResponse.ReportDto> reports = List.of(
                            StockDetailResponse.ReportDto.builder()
                                    .broker("NH투자증권").target("95,000원").stance("매수").build(),
                            StockDetailResponse.ReportDto.builder()
                                    .broker("한국투자증권").target("90,000원").stance("매수").build()
                    );

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
                .doOnError(e -> log.error("StockDetailService 에러: {}", e.getMessage(), e));
    }

    // 기술 지표만 반환하는 서비스 메소드
    public Mono<StockDetailResponse> getTechIndicators(String stockCode) {

        // 💡 종목 코드를 DB에서 찾아 API 심볼 형식으로 변환
        Stock stock = stockRepository.findByStockId(stockCode)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + stockCode));

        String apiSymbol = stockCode + ".KS";
        log.info("기술 지표 계산을 위해 API 심볼로 변환: {} -> {}", stockCode, apiSymbol);

        // Yahoo Finance API를 사용하여 기술 지표를 가져옴
        return yahooFinanceService.getTechnicalIndicators(apiSymbol)
                .subscribeOn(Schedulers.boundedElastic()) // 비동기 처리
                .map(techData -> {
                    log.info("Yahoo Finance 기술 지표 - RSI: {}, MACD: {}, MA20: {}",
                            techData.getRsi(), techData.getMacd(), techData.getMa20());

                    // 기술 지표 DTO 설정
                    StockDetailResponse.TechDto techDto = StockDetailResponse.TechDto.builder()
                            .rsi(techData.getRsi())
                            .macd(techData.getMacd())
                            .ma20(techData.getMa20())
                            .build();

                    // 💡 추가: 차트 데이터도 포함하여 프론트엔드 업데이트 로직에 대응
                    return StockDetailResponse.builder()
                            .tech(techDto)
                            .chart(techData.getChartData())
                            .build();
                });
    }
}