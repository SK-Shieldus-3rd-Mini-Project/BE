package com.roboadvisor.jeonbongjun.config;

import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource; // Resource 읽기용
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets; // UTF-8
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
                return; // 이미 데이터가 있으면 실행하지 않음
            }

            // 2. CSV 파일 읽기 (src/main/resources/krx_stocks.csv)
            log.info("🌱 Stock 마스터 데이터가 비어있습니다. 'krx_stocks.csv' 파일 로드를 시작합니다.");
            List<Stock> stockList = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new ClassPathResource("krx_stocks.csv").getInputStream(),
                            "EUC-KR" // KRX에서 받은 CSV는 보통 EUC-KR 인코딩입니다. (UTF-8이 아님)
                    )
            )) {

                String line = reader.readLine(); // 첫 번째 줄(헤더)은 건너뜀

                // 3. 한 줄씩 읽어서 Stock 엔티티로 변환
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(","); // 콤마로 분리

                    // KRX CSV 파일 형식에 맞게 인덱스 조정 (예시)
                    // [0]: 종목코드, [1]: 종목명, [2]: 시장구분, ...
                    String stockId = data[0].trim();
                    String stockName = data[1].trim();
                    String market = data[2].trim(); // KOSPI, KOSDAQ 등

                    // 우리 Stock 엔티티 형식에 맞게 빌드
                    Stock stock = Stock.builder()
                            .stockId(stockId)
                            .stockName(stockName)
                            .tickerSymbol(stockId)
                            .market(market)
                            .build();

                    stockList.add(stock);
                }

                // 4. 리스트에 담은 모든 Stock 엔티티를 DB에 한 번에 저장 (Batch Insert)
                stockRepository.saveAll(stockList);
                log.info("✅ Stock 마스터 데이터 {}개 적재 완료!", stockList.size());

            } catch (Exception e) {
                log.error("❌ Stock 마스터 데이터 로드 실패: {}", e.getMessage());
            }
        };
    }
}