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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient; // 추가
import reactor.core.publisher.Mono; // 추가
import com.fasterxml.jackson.core.JsonProcessingException; // 추가
import com.fasterxml.jackson.databind.ObjectMapper; // 추가
import reactor.core.scheduler.Schedulers;
import org.springframework.context.annotation.Lazy; // Lazy 임포트 추가

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j // Slf4j 로거 사용
@Service
//@RequiredArgsConstructor
public class ChatService {

    private final UserRepository userRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiResponseDetailRepository aiResponseDetailRepository;
    private final WebClient aiWebClient; // AI 서비스 통신용 WebClient 주입
    private final ObjectMapper objectMapper; // JSON 변환용 ObjectMapper 주입
    private final ChatService self; // 트랜잭션 분리를 위한 자기 자신 프록시 주입

    // --- 생성자 직접 작성 ---
    // self 파라미터에 @Lazy 추가
    public ChatService(UserRepository userRepository,
                       ChatSessionRepository chatSessionRepository,
                       ChatMessageRepository chatMessageRepository,
                       AiResponseDetailRepository aiResponseDetailRepository,
                       WebClient aiWebClient,
                       ObjectMapper objectMapper,
                       @Lazy ChatService self) { // <- 여기에 @Lazy 추가!
        this.userRepository = userRepository;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.aiResponseDetailRepository = aiResponseDetailRepository;
        this.aiWebClient = aiWebClient;
        this.objectMapper = objectMapper;
        this.self = self;
    }

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

    /**
     * 사용자 질문 저장 및 AI 서비스 비동기 호출 시작
     * @Transactional 어노테이션 제거: AI 호출은 별도 트랜잭션에서 처리
     */
    public void sendQuery(Integer sessionId, String question) {
        // 1. 사용자 질문 메시지 저장 (이 부분만 현재 트랜잭션에서 처리될 수 있음)
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        ChatMessage userMessage = ChatMessage.builder()
                .sender("USER")
                .content(question)
                .chatSession(session)
                .build();
        chatMessageRepository.save(userMessage); // 사용자 메시지 우선 저장

        // 2. AI 서비스 비동기 호출 및 결과 처리 시작 (block() 제거)
        callAiService(session.getSessionId().toString(), question)
                .publishOn(Schedulers.boundedElastic()) // DB 저장 등 블로킹 I/O 작업을 위한 스케줄러 지정
                .doOnSuccess(aiResponse -> {
                    if (aiResponse != null && aiResponse.getAnswer() != null) {
                        log.info("AI 응답 수신 성공 (세션 ID: {}). DB 저장 시작...", sessionId);
                        // AI 응답 DB 저장을 별도 트랜잭션으로 처리
                        self.saveAiMessageInNewTransaction(sessionId, aiResponse.getAnswer(), aiResponse);
                    } else {
                        log.warn("AI 응답이 null 이거나 답변이 없습니다 (세션 ID: {}). 에러 메시지 저장.", sessionId);
                        self.saveAiMessageInNewTransaction(sessionId, "AI 응답을 받지 못했습니다.", null);
                    }
                })
                .doOnError(error -> {
                    log.error("AI 서비스 호출 중 에러 발생 (세션 ID: {}): {}", sessionId, error.getMessage(), error);
                    // AI 호출 에러 시 DB 저장을 별도 트랜잭션으로 처리
                    self.saveAiMessageInNewTransaction(sessionId, "AI 응답 처리 중 오류가 발생했습니다.", null);
                })
                .subscribe(); // 비동기 작업 시작 (결과를 기다리지 않음)

        log.info("AI 서비스 호출 시작됨 (세션 ID: {}). 컨트롤러는 즉시 응답합니다.", sessionId);
        // 컨트롤러는 여기서 즉시 ResponseEntity.ok().build()를 반환함
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
        return aiWebClient.post()
                .uri("/api/ai/query")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AiResponseDto.class)
                // onErrorResume 제거 또는 수정: 에러 발생 시 Mono.error() 유지하여 doOnError에서 처리하도록 함
                .doOnError(error -> log.error("WebClient 에러 (세션 ID: {}): {}", sessionId, error.getMessage()));
        // .onErrorResume(e -> Mono.empty()); // 필요 시 빈 응답 대신 에러 전파 고려
    }

    /**
     * AI 응답 메시지를 별도의 트랜잭션으로 저장
     * @Transactional(propagation = Propagation.REQUIRES_NEW) : 항상 새 트랜잭션 시작
     */
    @Transactional // (propagation = Propagation.REQUIRES_NEW) // 필요 시 트랜잭션 전파 전략 명시
    public void saveAiMessageInNewTransaction(Integer sessionId, String content, AiResponseDto aiResponse) {
        log.info("별도 트랜잭션에서 AI 메시지 저장 시작 (세션 ID: {})...", sessionId);
        // 세션 엔티티를 다시 조회 (지연 로딩 문제 방지 및 트랜잭션 분리)
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("saveAiMessage: Session not found: " + sessionId));

        ChatMessage aiMessage = ChatMessage.builder()
                .sender("AI")
                .content(content)
                .chatSession(session) // 조회한 세션 엔티티 사용
                .build();

        // AiResponseDetail 생성 및 설정
        if (aiResponse != null) {
            try {
                AiResponseDetail detail = AiResponseDetail.builder()
                        .sourceCitations(objectMapper.writeValueAsString(aiResponse.getSources()))
                        // 기타 필드 매핑 (FastAPI 응답에 따라 추가)
                        .build();
                aiMessage.setAiResponseDetail(detail); // 연관관계 설정
            } catch (JsonProcessingException e) {
                log.error("AI 응답 sources JSON 변환 실패 (세션 ID: {}): {}", sessionId, e.getMessage(), e);
                // Detail 없이 메시지만 저장
            }
        }

        chatMessageRepository.save(aiMessage); // 메시지와 Detail(있다면) 저장
        log.info("AI 메시지 저장 완료 (세션 ID: {})", sessionId);
    }
}