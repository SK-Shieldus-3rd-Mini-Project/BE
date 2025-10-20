package com.roboadvisor.jeonbongjun.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_PORTFOLIO")
public class UserPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Integer portfolioId; // PK, INT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Column(name = "quantity", precision = 18, scale = 8) // DECIMAL(18, 8) 정도로 예시
    private BigDecimal quantity;

    @Column(name = "avg_purchase_price", precision = 18, scale = 2) // DECIMAL(18, 2)
    private BigDecimal avgPurchasePrice;

    @UpdateTimestamp // 데이터가 변경될 때마다 자동 갱신
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}