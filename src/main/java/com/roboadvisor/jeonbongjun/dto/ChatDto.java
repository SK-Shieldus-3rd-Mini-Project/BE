package com.roboadvisor.jeonbongjun.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class ChatDto {

    // 새 세션 시작 요청 DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionRequest {
        private String title;
    }

    // 채팅 세션 조회 응답 DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SessionResponse {
        private Integer sessionId;
        private String title;
        private LocalDateTime startTime;
        private List<MessageResponse> messages;
    }

    // 메시지 조회 응답 DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MessageResponse {
        private Integer messageId;
        private String sender;
        private String content;
        private LocalDateTime timestamp;
        private AiResponseDetailResponse aiResponseDetail;
    }

    // AI 응답 상세 정보 응답 DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AiResponseDetailResponse {
        private String economicDataUsed;
        private String sourceCitations;
        private String relatedChartsMetadata;
        private String relatedReports;
        private String ragModelVersion;
    }

    // 사용자 질문 전송 요청 DTO
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueryRequest {
        private String question;
    }

}