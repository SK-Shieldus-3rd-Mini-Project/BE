package com.roboadvisor.jeonbongjun.dto.deepsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HighlightDto {
    
    // JSON의 "content": [...] 배열을 매핑
    private List<String> content;
}