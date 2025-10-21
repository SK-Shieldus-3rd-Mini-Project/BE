package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.ChatDto;
import com.roboadvisor.jeonbongjun.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/chat/sessions")
public class ChatController {

    private final ChatService chatService;

    // 새 세션 시작
    @PostMapping
    public ResponseEntity<Integer> startSession(@PathVariable String userId, @RequestBody ChatDto.SessionRequest sessionRequest) {
        Integer sessionId = chatService.startSession(userId, sessionRequest.getTitle());
        return ResponseEntity.ok(sessionId);
    }

    // 채팅 세션 조회
    @GetMapping
    public ResponseEntity<List<ChatDto.SessionResponse>> listSessions(@PathVariable String userId) {
        List<ChatDto.SessionResponse> sessions = chatService.listSessions(userId);
        return ResponseEntity.ok(sessions);
    }

    // 특정 세션의 모든 메시지 조회
    @GetMapping("/{sessionId}/messages")
    public ResponseEntity<List<ChatDto.MessageResponse>> getMessages(@PathVariable String userId, @PathVariable Integer sessionId) {
        List<ChatDto.MessageResponse> messages = chatService.getMessages(sessionId);
        return ResponseEntity.ok(messages);
    }

    // 사용자 질문 전송 및 AI 답변
    @PostMapping("/{sessionId}/query")
    public ResponseEntity<Void> sendQuery(@PathVariable String userId, @PathVariable Integer sessionId, @RequestBody ChatDto.QueryRequest queryRequest) {
        chatService.sendQuery(sessionId, queryRequest.getQuestion());
        return ResponseEntity.ok().build();
    }
}
