package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    List<ChatMessage> findByChatSession_SessionId(Integer sessionId);  // 세션 ID로 메시지 목록 조회
}