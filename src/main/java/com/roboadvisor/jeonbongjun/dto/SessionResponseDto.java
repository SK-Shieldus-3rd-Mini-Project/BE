package com.roboadvisor.jeonbongjun.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionResponseDto {
    private String userId; // 1. Long -> String으로 변경
    private boolean isNewSession;
}