package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.SessionResponseDto;
import com.roboadvisor.jeonbongjun.service.SessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    @GetMapping("/init")
    public ResponseEntity<SessionResponseDto> initSession(HttpSession session) {
        // Spring이 자동으로 쿠키를 보고 Redis에서 세션을 찾아 주입해줍니다.
        SessionResponseDto response = sessionService.initializeSession(session);
        return ResponseEntity.ok(response);
    }
}