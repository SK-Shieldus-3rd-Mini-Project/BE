package com.roboadvisor.jeonbongjun.watchlist.dto;

import jakarta.validation.constraints.NotBlank;

public class WatchlistDto {

    // 관심 종목 추가 요청 DTO
    public record AddRequest(
            @NotBlank String stockId
    ) {
    }

    // 관심 종목 조회 응답 DTO
    public record ItemResponse(
            String stockId,
            String tickerSymbol,
            String stockName,
            String market,
            String addedAt
    ) {
    }

}