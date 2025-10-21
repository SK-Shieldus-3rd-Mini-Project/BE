//test로 넣은 파일입니다 테스트가 끝나면 지우면됩니다.

package com.roboadvisor.jeonbongjun.config; // 또는 다른 적절한 패키지

import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.repository.StockRepository; // StockRepository는 있어야 해
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component // 스프링 빈으로 등록해서 서버 시작 시 자동으로 실행되게 함
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StockRepository stockRepository; // Stock 데이터를 저장할 레포지토리 주입

    @Override
    public void run(String... args) throws Exception {
        // 서버가 시작되고 나서 딱 한 번 이 run() 메서드가 실행됨

        System.out.println("===== [DataInitializer] 시작: 초기 Stock 데이터 확인 및 적재 =====");

        // 1. 삼성전자 데이터 준비
        Stock samsung = Stock.builder()
                .stockId("005930")
                .stockName("삼성전자")
                .tickerSymbol("005930") // tickerSymbol도 넣어주는 게 좋음
                .market("KOSPI")
                .build();

        // 2. Apple 데이터 준비
        Stock apple = Stock.builder()
                .stockId("AAPL")
                .stockName("Apple")
                .tickerSymbol("AAPL") // tickerSymbol 추가
                .market("NASDAQ")
                .build();

        // 3. DB에 저장 (save는 없으면 INSERT, 있으면 UPDATE처럼 동작 - Id 기준)
        stockRepository.save(samsung);
        stockRepository.save(apple);

        System.out.println("===== [DataInitializer] 완료: 삼성전자, Apple 데이터 적재 완료 =====");
    }
}