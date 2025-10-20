package com.roboadvisor.jeonbongjun.config;

import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.entity.User;
import com.roboadvisor.jeonbongjun.entity.UserWatchlist;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import com.roboadvisor.jeonbongjun.repository.UserRepository;
import com.roboadvisor.jeonbongjun.repository.UserWatchlistRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration  // 설정 클래스임을 명시
public class SeedDataConfig {

    private final UserWatchlistRepository watchlistRepo;

    // 생성자 주입으로 리포지토리 객체 주입
    public SeedDataConfig(UserWatchlistRepository watchlistRepo) {
        this.watchlistRepo = watchlistRepo;
    }

    @Bean
    CommandLineRunner seed(UserRepository userRepo, StockRepository stockRepo) {
        return args -> {
            // 기본 사용자 데이터
            User user = userRepo.findById("u1").orElseGet(() ->
                    userRepo.save(User.builder()
                            .userId("u1")
                            .build())
            );

            // 기본 종목 데이터 (삼성전자, SK하이닉스)
            Stock samsung = stockRepo.findById("005930").orElseGet(() ->
                    stockRepo.save(Stock.builder()
                            .stockId("005930")
                            .tickerSymbol("005930")
                            .stockName("삼성전자")
                            .market("KOSPI")
                            .build())
            );

            Stock skhynix = stockRepo.findById("000660").orElseGet(() ->
                    stockRepo.save(Stock.builder()
                            .stockId("000660")
                            .tickerSymbol("000660")
                            .stockName("SK하이닉스")
                            .market("KOSPI")
                            .build())
            );

            // 관심종목 데이터 (u1 → 삼성전자)
            boolean alreadyExists = watchlistRepo.existsByUserAndStock(user, samsung);
            if (!alreadyExists) {
                watchlistRepo.save(UserWatchlist.builder()
                        .user(user)
                        .stock(samsung)
                        .build());
                System.out.println("🌱 관심종목 데이터 삽입 완료: u1 → 삼성전자");
            }

            // 관심종목 데이터 (u1 → SK하이닉스)
            alreadyExists = watchlistRepo.existsByUserAndStock(user, skhynix);
            if (!alreadyExists) {
                watchlistRepo.save(UserWatchlist.builder()
                        .user(user)
                        .stock(skhynix)
                        .build());
                System.out.println("🌱 관심종목 데이터 삽입 완료: u1 → SK하이닉스");
            }

            System.out.println("✅ Seed data inserted successfully!");
        };
    }
}