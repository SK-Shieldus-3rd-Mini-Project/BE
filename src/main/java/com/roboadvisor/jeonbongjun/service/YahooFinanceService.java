package com.roboadvisor.jeonbongjun.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class YahooFinanceService {

    private final WebClient webClient;

    public YahooFinanceService() {
        // HttpClient 설정: 타임아웃 증가 및 SSL 안전 설정
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000) // 30초
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
                );

        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build();
    }

    /**
     * Yahoo Finance에서 차트 데이터 + 가격 정보를 가져와 기술 지표 계산
     * @param symbol 종목 코드 (예: "000020.KS")
     * @return TechnicalIndicators DTO (가격 정보 포함)
     */
    public Mono<TechnicalIndicators> getTechnicalIndicators(String symbol) {
        String url = String.format(
                "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1d&range=3mo",
                symbol
        );

        log.info("Yahoo Finance API 호출: {}", url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30)) // 추가 타임아웃
                .map(response -> {
                    try {
                        log.debug("Yahoo Finance 응답 수신 성공");

                        // Yahoo Finance 응답 파싱
                        JsonNode result = response.path("chart").path("result").get(0);

                        if (result == null || result.isMissingNode()) {
                            log.warn("Yahoo Finance 응답에 result가 없음");
                            return TechnicalIndicators.empty();
                        }

                        JsonNode quotes = result.path("indicators").path("quote").get(0);
                        JsonNode timestamps = result.path("timestamp");
                        JsonNode meta = result.path("meta");

                        if (timestamps == null || timestamps.size() == 0) {
                            log.warn("Yahoo Finance 응답에 timestamp 데이터 없음");
                            return TechnicalIndicators.empty();
                        }

                        // 최신 가격 정보 추출
                        double currentPrice = meta.path("regularMarketPrice").asDouble(0.0);
                        double previousClose = meta.path("chartPreviousClose").asDouble(0.0);
                        double changeAmount = currentPrice - previousClose;
                        double changePercent = previousClose > 0 ? (changeAmount / previousClose) * 100 : 0.0;

                        // 시계열 데이터 생성
                        BarSeries series = new BaseBarSeriesBuilder()
                                .withName(symbol)
                                .build();

                        // 데이터 추가 (최대 100개)
                        int size = Math.min(timestamps.size(), 100);
                        int addedCount = 0;
                        Double lastOpen = null, lastHigh = null, lastLow = null, lastClose = null;

                        for (int i = 0; i < size; i++) {
                            long timestamp = timestamps.get(i).asLong();
                            Double open = getDoubleValue(quotes.path("open").get(i));
                            Double high = getDoubleValue(quotes.path("high").get(i));
                            Double low = getDoubleValue(quotes.path("low").get(i));
                            Double close = getDoubleValue(quotes.path("close").get(i));
                            Long volume = quotes.path("volume").get(i).asLong(0);

                            if (open != null && high != null && low != null && close != null) {
                                ZonedDateTime date = ZonedDateTime.ofInstant(
                                        Instant.ofEpochSecond(timestamp),
                                        ZoneId.systemDefault()
                                );
                                series.addBar(date, open, high, low, close, volume);
                                addedCount++;

                                // 가장 최근 OHLC 저장
                                if (i == size - 1) {
                                    lastOpen = open;
                                    lastHigh = high;
                                    lastLow = low;
                                    lastClose = close;
                                }
                            }
                        }

                        log.info("Yahoo Finance 데이터 추가 완료: {}개", addedCount);

                        // 기술 지표 계산
                        TechnicalIndicators baseIndicators = calculateIndicators(series);

                        // 가격 정보를 포함한 새로운 객체 생성
                        return TechnicalIndicators.builder()
                                .rsi(baseIndicators.getRsi())
                                .macd(baseIndicators.getMacd())
                                .ma20(baseIndicators.getMa20())
                                .chartData(baseIndicators.getChartData())
                                .price((long) currentPrice)
                                .changeAmount((long) changeAmount)
                                .changePercent(changePercent)
                                .open(lastOpen != null ? lastOpen.longValue() : 0L)
                                .high(lastHigh != null ? lastHigh.longValue() : 0L)
                                .low(lastLow != null ? lastLow.longValue() : 0L)
                                .build();

                    } catch (Exception e) {
                        log.error("Yahoo Finance 데이터 파싱 실패: {}", e.getMessage(), e);
                        return TechnicalIndicators.empty();
                    }
                })
                .onErrorResume(e -> {
                    log.error("Yahoo Finance API 호출 실패: {}", e.getMessage());
                    log.error("상세 에러: ", e);
                    return Mono.just(TechnicalIndicators.empty());
                });
    }

    /**
     * TA4J를 사용하여 기술 지표 계산
     */
    private TechnicalIndicators calculateIndicators(BarSeries series) {
        if (series.getBarCount() < 26) {
            log.warn("데이터가 부족하여 기술 지표 계산 불가 (최소 26개 필요)");
            return TechnicalIndicators.empty();
        }

        try {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
            int endIndex = series.getEndIndex();

            // RSI (14일)
            RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);
            double rsi = rsiIndicator.getValue(endIndex).doubleValue();

            // MACD (12, 26, 9)
            MACDIndicator macdIndicator = new MACDIndicator(closePrice, 12, 26);
            double macd = macdIndicator.getValue(endIndex).doubleValue();

            // SMA (20일 이동평균선)
            SMAIndicator smaIndicator = new SMAIndicator(closePrice, 20);
            double ma20 = smaIndicator.getValue(endIndex).doubleValue();

            // 차트 데이터 (최근 30개)
            List<Double> chartData = new ArrayList<>();
            int startIdx = Math.max(0, endIndex - 29);
            for (int i = startIdx; i <= endIndex; i++) {
                chartData.add(series.getBar(i).getClosePrice().doubleValue());
            }

            log.info("기술 지표 계산 완료 - RSI: {}, MACD: {}, MA20: {}", rsi, macd, ma20);

            return TechnicalIndicators.builder()
                    .rsi(rsi)
                    .macd(macd)
                    .ma20(ma20)
                    .chartData(chartData)
                    .build();

        } catch (Exception e) {
            log.error("기술 지표 계산 중 에러: {}", e.getMessage(), e);
            return TechnicalIndicators.empty();
        }
    }

    /**
     * JsonNode에서 Double 값 안전하게 추출
     */
    private Double getDoubleValue(JsonNode node) {
        if (node == null || node.isNull() || !node.isNumber()) {
            return null;
        }
        return node.asDouble();
    }

    /**
     * 기술 지표 DTO
     */
    @lombok.Builder
    @lombok.Data
    public static class TechnicalIndicators {
        private double rsi;
        private double macd;
        private double ma20;
        private List<Double> chartData;

        // 가격 정보 추가
        private long price;
        private long changeAmount;
        private double changePercent;
        private long open;
        private long high;
        private long low;

        public static TechnicalIndicators empty() {
            return TechnicalIndicators.builder()
                    .rsi(0.0)
                    .macd(0.0)
                    .ma20(0.0)
                    .chartData(new ArrayList<>())
                    .price(0L)
                    .changeAmount(0L)
                    .changePercent(0.0)
                    .open(0L)
                    .high(0L)
                    .low(0L)
                    .build();
        }
    }
}