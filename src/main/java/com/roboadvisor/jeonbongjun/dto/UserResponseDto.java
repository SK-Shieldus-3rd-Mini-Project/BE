package com.roboadvisor.jeonbongjun.dto;

import com.roboadvisor.jeonbongjun.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponseDto {
    private String userId;
    private LocalDateTime createdAt;

    // 엔티티를 DTO로 변환하는 생성자
    public UserResponseDto(User user) {
        this.userId = user.getUserId();
        this.createdAt = user.getCreatedAt();
    }
}