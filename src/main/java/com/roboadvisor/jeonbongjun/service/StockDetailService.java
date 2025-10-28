package com.roboadvisor.jeonbongjun.service;

import com.roboadvisor.jeonbongjun.dto.StockDetailResponse;
import com.roboadvisor.jeonbongjun.dto.NewsDto;
import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDetailService {

    private final StockRepository stockRepository;
    private final NewsService newsService;
    private final YahooFinanceService yahooFinanceService;

    public Mono<StockDetailResponse> getStockDetail(String stockCode) {

        Stock stock = stockRepository.findByStockId(stockCode)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + stockCode));

        String apiSymbol = stockCode + ".KS";
        String stockName = stock.getStockName();

        // Yahoo Finance로 모든 데이터 가져오기 (가격 + 기술 지표)
        Mono<YahooFinanceService.TechnicalIndicators> techMono =
                yahooFinanceService.getTechnicalIndicators(apiSymbol)
                        .subscribeOn(Schedulers.boundedElastic());

        Mono<List<NewsDto>> newsMono = newsService.searchNews(stockName)
                .subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(techMono, newsMono)
                .map(tuple -> {
                    YahooFinanceService.TechnicalIndicators techData = tuple.getT1();
                    List<NewsDto> newsData = tuple.getT2();

                    log.debug("Yahoo Finance Data: price={}, changePct={}, RSI={}, MACD={}, MA20={}",
                            techData.getPrice(), techData.getChangePercent(),
                            techData.getRsi(), techData.getMacd(), techData.getMa20());

                    // OHLC 데이터
                    StockDetailResponse.OhlcDto ohlc = StockDetailResponse.OhlcDto.builder()
                            .open(techData.getOpen())
                            .high(techData.getHigh())
                            .low(techData.getLow())
                            .build();

                    // 기술 지표
                    StockDetailResponse.TechDto tech = StockDetailResponse.TechDto.builder()
                            .rsi(techData.getRsi())
                            .macd(techData.getMacd())
                            .ma20(techData.getMa20())
                            .build();

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
                            .price(techData.getPrice())
                            .changePct(techData.getChangePercent())
                            .changeAmt(techData.getChangeAmount())
                            .ohlc(ohlc)
                            .tech(tech)
                            .chart(techData.getChartData())
                            .news(newsTitles)
                            .reports(reports)
                            .build();
                })
                .doOnError(e -> log.error("StockDetailService 에러: {}", e.getMessage(), e));
    }

    // 기술 지표만 반환하는 서비스 메소드
    public Mono<StockDetailResponse> getTechIndicators(String stockCode) {

        Stock stock = stockRepository.findByStockId(stockCode)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + stockCode));

        String apiSymbol = stockCode + ".KS";
        log.info("기술 지표 계산을 위해 API 심볼로 변환: {} -> {}", stockCode, apiSymbol);

        return yahooFinanceService.getTechnicalIndicators(apiSymbol)
                .subscribeOn(Schedulers.boundedElastic())
                .map(techData -> {
                    log.info("Yahoo Finance 기술 지표 - RSI: {}, MACD: {}, MA20: {}",
                            techData.getRsi(), techData.getMacd(), techData.getMa20());

                    StockDetailResponse.TechDto techDto = StockDetailResponse.TechDto.builder()
                            .rsi(techData.getRsi())
                            .macd(techData.getMacd())
                            .ma20(techData.getMa20())
                            .build();

                    return StockDetailResponse.builder()
                            .tech(techDto)
                            .chart(techData.getChartData())
                            .build();
                });
    }
}