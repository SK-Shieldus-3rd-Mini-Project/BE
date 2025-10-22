package com.roboadvisor.jeonbongjun.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class AiResponseDto {
    private String sessionId;
    private String question;
    private String answer;
    private String category;
    private List<Map<String, String>> sources; // FastAPI의 Source 모델에 맞춰 Map 사용
    private String timestamp;

    // source 예시: {"title": "NH투자증권 리포트", "securities_firm": "NH투자증권", "date": "2025-10-15"}
}