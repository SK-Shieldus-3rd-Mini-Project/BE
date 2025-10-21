package com.roboadvisor.jeonbongjun.config;

import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SeedDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SeedDataConfig.class);

    @Bean
    CommandLineRunner loadStockData(StockRepository stockRepository) {
        return args -> {
            // 1. DB에 이미 종목 데이터가 있는지 확인
            long count = stockRepository.count();
            if (count > 0) {
                log.info("✅ Stock 마스터 데이터가 이미 존재합니다. ({}개). 배치를 건너뜁니다.", count);
                return;
            }

            // 2. CSV 파일 읽기
            log.info("🌱 Stock 마스터 데이터가 비어있습니다. 'krx_stocks.csv' 파일 로드를 시작합니다.");
            List<Stock> stockList = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new ClassPathResource("krx_stocks.csv").getInputStream(),
                            "EUC-KR" // KRX CSV 파일 인코딩
                    )
            )) {

                String line = reader.readLine(); // 첫 번째 줄(헤더)은 건너뜀

                // 3. 한 줄씩 읽어서 Stock 엔티티로 변환
                while ((line = reader.readLine()) != null) {

                    List<String> dataList = parseCsvLine(line);
                    String[] data = dataList.toArray(new String[0]);

                    if (data.length < 7) {
                        log.warn("CSV 라인 형식이 올바르지 않습니다. (컬럼 부족): {}", line);
                        continue;
                    }

                    try {
                        // DB 컬럼에 맞게 CSV 데이터 추출 (따옴표는 parseCsvLine에서 이미 제거됨)
                        String stockId = data[1].trim();     // 2번 컬럼 (종목 코드)
                        String market = data[6].trim();      // 7번 컬럼 (시장 구분)
                        String stockName = data[3].trim();   // 4번 컬럼 (종목명/약칭)
                        String tickerSymbol = data[0].trim(); // 1번 컬럼 (표준 코드)

                        // 우리 Stock 엔티티 형식에 맞게 빌드
                        Stock stock = Stock.builder()
                                .stockId(stockId)         // (CSV 2번)
                                .market(market)         // (CSV 7번)
                                .stockName(stockName)   // (CSV 4번)
                                .tickerSymbol(tickerSymbol) // (CSV 1번)
                                .build();

                        stockList.add(stock);

                    } catch (ArrayIndexOutOfBoundsException e) {
                        // 혹시 모를 인덱스 오류 방지
                        log.error("❌ CSV 파싱 인덱스 오류: {}", line, e);
                    }
                }

                // 4. 리스트에 담은 모든 Stock 엔티티를 DB에 한 번에 저장 (Batch Insert)
                stockRepository.saveAll(stockList);
                log.info("✅ Stock 마스터 데이터 {}개 적재 완료!", stockList.size());

            } catch (Exception e) {
                log.error("❌ Stock 마스터 데이터 로드 실패: {}", e.getMessage());
            }
        };
    }

    /**
     * [새로 추가된 메소드]
     * 따옴표(")로 묶인 필드 내부의 쉼표(,)를 무시하는 간단한 CSV 라인 파서
     */
    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                // 따옴표 처리
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    currentField.append('\"');
                    i++; // 다음 문자(따옴표) 건너뛰기
                } else {
                    // 따옴표 시작 또는 끝
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // 필드 구분자 (따옴표 안에 있지 않을 때만)
                fields.add(currentField.toString());
                currentField.setLength(0); // 현재 필드 초기화
            } else {
                // 일반 문자
                currentField.append(c);
            }
        }
        // 마지막 필드 추가
        fields.add(currentField.toString());

        return fields;
    }
}