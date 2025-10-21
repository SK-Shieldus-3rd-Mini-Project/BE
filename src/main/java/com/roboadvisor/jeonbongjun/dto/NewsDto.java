package com.roboadvisor.jeonbongjun.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsDto {
    private String id;
    private String title;
    private String press; // 언론사
    private String summary; // 요약 본문
    private String fullContent; // (더보기) 전체 본문
    private String url; // 원문 URL
}