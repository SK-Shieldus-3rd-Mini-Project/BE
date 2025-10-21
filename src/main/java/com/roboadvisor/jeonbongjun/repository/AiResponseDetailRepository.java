package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.AiResponseDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiResponseDetailRepository extends JpaRepository<AiResponseDetail, Integer> {
    Optional<AiResponseDetail> findByChatMessage_MessageId(Integer messageId); // 메시지 ID로 AI 응답 상세 조회
}