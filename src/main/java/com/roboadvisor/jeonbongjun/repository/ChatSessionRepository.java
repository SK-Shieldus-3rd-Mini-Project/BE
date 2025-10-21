package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Integer> {
    List<ChatSession> findByUser_UserId(String userId); // 사용자 ID로 채팅 세션 목록 조회
}



