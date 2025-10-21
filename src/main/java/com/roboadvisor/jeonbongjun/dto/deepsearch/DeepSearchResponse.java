package com.roboadvisor.jeonbongjun.dto.deepsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

/**
 * 딥서치 API의 최상위 응답 래퍼
 * {"total_items": 410, "data": [...]}
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 모르는 필드는 무시
public class DeepSearchResponse {
    private int total_items;
    private List<DeepSearchArticle> data;
}