package com.roboadvisor.jeonbongjun.dto.deepsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty; // 1. import 추가
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeepSearchArticle {

    private String id;
    private String title;
    private String publisher;

    // --- [수정된 필드] ---

    // 2. "summary": "..." (긴 본문 요약)
    private String summary;

    // 3. "highlight": { "content": ["..."] } (카드용 짧은 요약)
    private HighlightDto highlight;

    // 4. "content_url": "http://..."
    @JsonProperty("content_url") // JSON의 "content_url"을 이 필드로 매핑
    private String contentUrl;

}