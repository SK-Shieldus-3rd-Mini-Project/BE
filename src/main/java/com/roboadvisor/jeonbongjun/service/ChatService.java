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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatService {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiResponseDetailRepository aiResponseDetailRepository;
    private final WebClient aiWebClient; // ★ AI 서비스 통신용 WebClient
    private final ObjectMapper objectMapper;
    private final ChatService self;

    // 생성자
    public ChatService(UserRepository userRepository,
                       ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       AiResponseDetailRepository aiResponseDetailRepository,
                       @Qualifier("aiWebClient") WebClient aiWebClient,
                       ObjectMapper objectMapper,
                       @Lazy ChatService self) {
        this.userRepository = userRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.aiResponseDetailRepository = aiResponseDetailRepository;
        this.aiWebClient = aiWebClient;
        this.objectMapper = objectMapper;
        this.self = self;
    }

    /**
     * 새 세션 시작
     */
    @Transactional
    public Integer startSession(String userId, String title) {
        ChatSession session = new ChatSession();
        session.setUser(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        session.setTitle(title);
        session.setStartTime(LocalDateTime.now());
        ChatSession savedSession = chatSessionRepository.save(session);
        return savedSession.getSessionId();
    }

    /**
     * 채팅 세션 목록 조회
     */
    public List<ChatDto.SessionResponse> listSessions(String userId) {
        List<ChatSession> sessions = chatSessionRepository.findByUser_UserId(userId);
        return sessions.stream().map(session -> {
            List<ChatDto.MessageResponse> messages = getMessages(session.getSessionId());
            return new ChatDto.SessionResponse(
                    session.getSessionId(),
                    session.getTitle(),
                    session.getStartTime(),
                    messages
            );
        }).toList();
    }

    /**
     * 특정 세션의 메시지 조회
     */
    @Transactional
    public List<ChatDto.MessageResponse> getMessages(Integer sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findByChatSession_SessionId(sessionId);
        return messages.stream().map(message -> {
            AiResponseDetail aiResponseDetail = aiResponseDetailRepository
                    .findByChatMessage_MessageId(message.getMessageId())
                    .orElse(null);
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

    /**
     * 사용자 질문 저장 및 AI 서비스 비동기 호출
     */
    public void sendQuery(Integer sessionId, String question) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // ★ 1. 사용자 질문 저장 (USER 메시지)
        ChatMessage userMessage = ChatMessage.builder()
                .sender("USER")
                .content(question)
                .chatSession(session)
                .build();
        chatMessageRepository.save(userMessage);

        // ★ 2. AI 서비스 비동기 호출
        callAiService(session.getSessionId().toString(), question)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(aiResponse -> {
                    if (aiResponse != null && aiResponse.getAnswer() != null) {
                        log.info("AI 응답 수신 성공 (세션 ID: {}). DB 저장 시작...", sessionId);
                        self.saveAiMessageInNewTransaction(sessionId, aiResponse);
                    } else {
                        log.warn("AI 응답이 null 또는 답변이 없습니다 (세션 ID: {}).", sessionId);
                        self.saveErrorMessageInNewTransaction(sessionId, "AI 응답을 받지 못했습니다.");
                    }
                })
                .doOnError(error -> {
                    log.error("AI 서비스 호출 중 에러 발생 (세션 ID: {}): {}", sessionId, error.getMessage(), error);
                    self.saveErrorMessageInNewTransaction(sessionId, "AI 응답 처리 중 오류가 발생했습니다.");
                })
                .subscribe();

        log.info("AI 서비스 호출 시작됨 (세션 ID: {}). 컨트롤러는 즉시 응답합니다.", sessionId);
    }

    /**
     * AI 서비스 호출 (비동기 Mono 반환)
     */
    private Mono<AiResponseDto> callAiService(String sessionId, String question) {
        Map<String, String> requestBody = Map.of(
                "session_id", sessionId,
                "question", question
        );

        log.info("AI 서비스 호출 (세션 ID: {}, 질문: {})...", sessionId, question);

        // ★ FastAPI의 /ai/query 엔드포인트 호출
        return aiWebClient.post()
                .uri("/ai/query")  // WebClientConfig에 설정된 baseUrl 기준
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AiResponseDto.class)
                .doOnError(error -> log.error("WebClient 에러 (세션 ID: {}): {}", sessionId, error.getMessage()));
    }

    /**
     * AI 응답 메시지를 별도의 트랜잭션으로 저장
     */
    @Transactional
    public void saveAiMessageInNewTransaction(Integer sessionId, AiResponseDto aiResponse) {
        log.info("별도 트랜잭션에서 AI 메시지 저장 시작 (세션 ID: {})...", sessionId);
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("saveAiMessage: Session not found: " + sessionId));

        // ★ AI 메시지 생성
        ChatMessage aiMessage = ChatMessage.builder()
                .sender("AI")
                .content(aiResponse.getAnswer())
                .chatSession(session)
                .build();

        // ★ AI 응답 상세 정보 저장 (출처 등)
        if (aiResponse.getSources() != null && !aiResponse.getSources().isEmpty()) {
            try {
                AiResponseDetail detail = AiResponseDetail.builder()
                        .sourceCitations(objectMapper.writeValueAsString(aiResponse.getSources()))
                        .ragModelVersion(aiResponse.getCategory()) // 카테고리를 버전으로 활용
                        .build();
                aiMessage.setAiResponseDetail(detail); // 양방향 관계 설정
            } catch (JsonProcessingException e) {
                log.error("AI 응답 sources JSON 변환 실패 (세션 ID: {}): {}", sessionId, e.getMessage(), e);
            }
        }

        chatMessageRepository.save(aiMessage);
        log.info("AI 메시지 저장 완료 (세션 ID: {})", sessionId);
    }

    /**
     * 에러 메시지 저장
     */
    @Transactional
    public void saveErrorMessageInNewTransaction(Integer sessionId, String errorMessage) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        ChatMessage errorMsg = ChatMessage.builder()
                .sender("AI")
                .content(errorMessage)
                .chatSession(session)
                .build();

        chatMessageRepository.save(errorMsg);
        log.info("에러 메시지 저장 완료 (세션 ID: {})", sessionId);
    }
}