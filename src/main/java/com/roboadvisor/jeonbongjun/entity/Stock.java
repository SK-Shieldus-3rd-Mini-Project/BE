package com.roboadvisor.jeonbongjun.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "STOCK")
public class Stock {

    @Id
    @Column(name = "stock_id")
    private String stockId; // PK, VARCHAR (종목 코드)

    @Column(name = "ticker_symbol")
    private String tickerSymbol;

    @Column(name = "stock_name")
    private String stockName;

    @Column(name = "market")
    private String market;

    // Stock 정보가 포트폴리오에서 참조됨
    @OneToMany(mappedBy = "stock")
    private List<UserPortfolio> portfolioList = new ArrayList<>();

    // Stock 정보가 관심 목록에서 참조됨
    @OneToMany(mappedBy = "stock")
    private List<UserWatchlist> watchList = new ArrayList<>();
}