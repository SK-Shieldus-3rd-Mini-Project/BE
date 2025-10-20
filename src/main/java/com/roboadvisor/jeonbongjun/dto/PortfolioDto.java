package com.roboadvisor.jeonbongjun.dto;

import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.entity.UserPortfolio;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PortfolioDto {

    @Getter
    @NoArgsConstructor
    public static class AddRequest {
        private String stockId;
        private BigDecimal quantity;
        private BigDecimal avgPurchasePrice;
    }

    @Getter
    @NoArgsConstructor
    public static class UpdateRequest {
        private BigDecimal quantity;
        private BigDecimal avgPurchasePrice;
    }

    @Getter
    @Builder
    public static class Response {
        private Integer portfolioId;
        private String stockId;
        private String stockName; // N+1 해결로 가져온 Stock 이름
        private BigDecimal quantity;
        private BigDecimal avgPurchasePrice;
        private LocalDateTime lastUpdated;

        public static Response fromEntity(UserPortfolio portfolio) {
            // Fetch Join으로 조회된 Stock 엔티티를 사용
            Stock stock = portfolio.getStock();
            
            return Response.builder()
                    .portfolioId(portfolio.getPortfolioId())
                    .stockId(stock != null ? stock.getStockId() : null)
                    .stockName(stock != null ? stock.getStockName() : null)
                    .quantity(portfolio.getQuantity())
                    .avgPurchasePrice(portfolio.getAvgPurchasePrice())
                    .lastUpdated(portfolio.getLastUpdated())
                    .build();
        }
    }
}