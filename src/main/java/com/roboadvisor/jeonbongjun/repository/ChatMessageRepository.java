package com.roboadvisor.jeonbongjun.repository;// In jeonbongjun/repository/ChatMessageRepository.java
import com.roboadvisor.jeonbongjun.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    // Keep the original method if needed elsewhere
    List<ChatMessage> findByChatSession_SessionId(Integer sessionId);

    // Add this new method with JOIN FETCH
    @Query("SELECT cm FROM ChatMessage cm LEFT JOIN FETCH cm.aiResponseDetail WHERE cm.chatSession.sessionId = :sessionId ORDER BY cm.timestamp ASC")
    List<ChatMessage> findByChatSession_SessionIdWithDetails(@Param("sessionId") Integer sessionId);
}