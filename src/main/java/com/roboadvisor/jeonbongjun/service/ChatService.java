package com.roboadvisor.jeonbongjun.service;

import com.roboadvisor.jeonbongjun.dto.ChatDto;
import com.roboadvisor.jeonbongjun.entity.ChatSession;
import com.roboadvisor.jeonbongjun.entity.ChatMessage;
import com.roboadvisor.jeonbongjun.entity.AiResponseDetail;
import com.roboadvisor.jeonbongjun.repository.ChatSessionRepository;
import com.roboadvisor.jeonbongjun.repository.ChatMessageRepository;
import com.roboadvisor.jeonbongjun.repository.AiResponseDetailRepository;
import com.roboadvisor.jeonbongjun.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiResponseDetailRepository aiResponseDetailRepository;

    @Transactional
    public Integer startSession(String userId, String title) {
        // 새로운 세션 시작
        ChatSession session = new ChatSession();
        session.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        session.setTitle(title);
        session.setStartTime(LocalDateTime.now());

        ChatSession savedSession = chatSessionRepository.save(session);
        return savedSession.getSessionId();
    }

    // 채팅 세션 조회
    public List<ChatDto.SessionResponse> listSessions(String userId) {
        // 사용자 ID로 세션 리스트 조회
        List<ChatSession> sessions = chatSessionRepository.findByUser_UserId(userId);

        // 세션 정보를 DTO로 변환하여 반환
        return sessions.stream().map(session -> {
            List<ChatDto.MessageResponse> messages = getMessages(session.getSessionId()); // 세션에 포함된 메시지 조회

            return new ChatDto.SessionResponse(
                    session.getSessionId(),
                    session.getTitle(),
                    session.getStartTime(),
                    messages
            );
        }).toList();
    }

    @Transactional
    public List<ChatDto.MessageResponse> getMessages(Integer sessionId) {
        // 세션에 속한 메시지 조회
        List<ChatMessage> messages = chatMessageRepository.findByChatSession_SessionId(sessionId);

        return messages.stream().map(message -> {
            AiResponseDetail aiResponseDetail = aiResponseDetailRepository.findByChatMessage_MessageId(message.getMessageId()).orElse(null);
            return new ChatDto.MessageResponse(
                    message.getMessageId(),
                    message.getSender(),
                    message.getContent(),
                    message.getTimestamp(),
                    aiResponseDetail != null ? new ChatDto.AiResponseDetailResponse(
                            aiResponseDetail.getEconomicDataUsed(),
                            aiResponseDetail.getSourceCitations(),
                            aiResponseDetail.getRelatedChartsMetadata(),
                            aiResponseDetail.getRelatedReports(),
                            aiResponseDetail.getRagModelVersion()) : null
            );
        }).toList();
    }

    @Transactional
    public void sendQuery(Integer sessionId, String question) {
        // 사용자의 질문 메시지 저장
        ChatSession session = chatSessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));

        ChatMessage userMessage = new ChatMessage();
        userMessage.setSender("USER");
        userMessage.setContent(question);
        userMessage.setChatSession(session);

        ChatMessage savedUserMessage = chatMessageRepository.save(userMessage);

        // AI의 응답 (단순한 하드코딩 또는 외부 API로 처리)
        String aiResponse = "AI's response to: " + question;

        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setSender("AI");
        aiMessage.setContent(aiResponse);
        aiMessage.setChatSession(session);

        ChatMessage savedAiMessage = chatMessageRepository.save(aiMessage);

        // AI 응답 상세 정보 저장
        AiResponseDetail aiResponseDetail = new AiResponseDetail();
        aiResponseDetail.setChatMessage(savedAiMessage);
        aiResponseDetail.setEconomicDataUsed("economic data...");
        aiResponseDetail.setSourceCitations("source citations...");
        aiResponseDetail.setRelatedChartsMetadata("chart metadata...");
        aiResponseDetail.setRelatedReports("reports...");
        aiResponseDetail.setRagModelVersion("RAG model version...");

        aiResponseDetailRepository.save(aiResponseDetail);
    }
}