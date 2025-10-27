package com.roboadvisor.jeonbongjun.dto;

import jakarta.validation.constraints.NotBlank; // [추가] Validation 임포트
import lombok.Builder;
import lombok.Getter;
// import lombok.RequiredArgsConstructor; // [제거]

import java.util.List;

public class WatchlistDto {

    // 관심 목록 추가 요청 DTO
    @Getter
    public static class AddRequest {

        @NotBlank(message = "stockId는 필수입니다.")
        private String stockId;

        public AddRequest() {
        }
    }

    @Getter
    @Builder
    public static class ItemResponse {
        private String stockId;
        private String stockName;
        private Long price;
        private Double changePct;
    }
}