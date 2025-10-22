package com.roboadvisor.jeonbongjun.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roboadvisor.jeonbongjun.dto.AiResponseDto;
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
import org.springframework.web.reactive.function.client.WebClient; // 추가
import reactor.core.publisher.Mono; // 추가
import com.fasterxml.jackson.core.JsonProcessingException; // 추가
import com.fasterxml.jackson.databind.ObjectMapper; // 추가

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiResponseDetailRepository aiResponseDetailRepository;
    private final WebClient aiWebClient; // AI 서비스 통신용 WebClient 주입
    private final ObjectMapper objectMapper; // JSON 변환용 ObjectMapper 주입

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
        // 1. 사용자 질문 메시지 저장
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        ChatMessage userMessage = ChatMessage.builder()
                .sender("USER")
                .content(question)
                .chatSession(session)
                .build();
        chatMessageRepository.save(userMessage);

        // 2. AI 서비스(FastAPI) 호출
        AiResponseDto aiResponse = callAiService(session.getSessionId().toString(), question) // 세션 ID를 String으로 변환
                .block(); // 비동기 응답을 동기적으로 기다림 (실제 운영 환경에서는 비동기 처리 고려)

        if (aiResponse == null || aiResponse.getAnswer() == null) {
            // AI 응답 실패 처리 (예: 기본 응답 또는 에러 로그)
            saveAiMessage(session, "AI 응답을 처리하는 중 오류가 발생했습니다.", null);
            // 에러 로깅 추가
            System.err.println("AI service call failed or returned null response for session: " + sessionId);
            return;
        }

        // 3. AI 응답 메시지 및 상세 정보 저장
        saveAiMessage(session, aiResponse.getAnswer(), aiResponse);
    }

    // AI 서비스 호출 로직 분리
    private Mono<AiResponseDto> callAiService(String sessionId, String question) {
        // FastAPI 요청 본문 생성 (main.py의 QueryRequest 모델 참고)
        Map<String, String> requestBody = Map.of(
                "session_id", sessionId,
                "question", question
        );

        return aiWebClient.post()
                .uri("/api/ai/query") // FastAPI 엔드포인트 경로
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AiResponseDto.class)
                .doOnError(error -> {
                    // 에러 로깅 추가
                    System.err.println("Error calling AI service: " + error.getMessage());
                })
                .onErrorResume(e -> Mono.empty()); // 오류 발생 시 빈 Mono 반환 (null 대신)
    }

    // AI 메시지 저장 로직 분리
    private void saveAiMessage(ChatSession session, String content, AiResponseDto aiResponse) {
        ChatMessage aiMessage = ChatMessage.builder()
                .sender("AI")
                .content(content)
                .chatSession(session)
                .build();

        // AiResponseDetail 생성 및 설정
        if (aiResponse != null) {
            try {
                AiResponseDetail detail = AiResponseDetail.builder()
                        // sources 필드는 JSON 문자열로 저장 (List<Map> -> String)
                        .sourceCitations(objectMapper.writeValueAsString(aiResponse.getSources()))
                        // FastAPI 응답에는 economicDataUsed, relatedChartsMetadata, relatedReports, ragModelVersion이 없으므로 null 또는 기본값 처리
                        .economicDataUsed(null) // 필요시 FastAPI 응답에 추가 후 매핑
                        .relatedChartsMetadata(null) // 필요시 FastAPI 응답에 추가 후 매핑
                        .relatedReports(null) // 필요시 FastAPI 응답에 추가 후 매핑
                        .ragModelVersion(null) // 필요시 FastAPI 응답에 추가 후 매핑
                        .build();
                // ChatMessage와 AiResponseDetail 양방향 연관관계 설정
                aiMessage.setAiResponseDetail(detail);
            } catch (JsonProcessingException e) {
                // JSON 변환 오류 로깅
                System.err.println("Error converting sources to JSON string: " + e.getMessage());
                // detail 없이 aiMessage만 저장하거나, 기본 detail 정보 저장
            }
        }

        chatMessageRepository.save(aiMessage); // AiResponseDetail도 CascadeType.ALL로 함께 저장됨
    }
}