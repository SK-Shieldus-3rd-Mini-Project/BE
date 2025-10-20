package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.UserResponseDto;
import com.roboadvisor.jeonbongjun.entity.User;
import com.roboadvisor.jeonbongjun.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /**
     * POST /api/users
     * - 익명 사용자를 생성하고 생성된 userId를 반환합니다.
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createGuestUser() {
        User newUser = new User();

        // 1단계에서 추가한 @PrePersist 덕분에
        // save()가 호출되기 직전에 UUID가 자동으로 할당됩니다.
        User savedUser = userRepository.save(newUser);

        // 프론트엔드가 localStorage에 저장할 수 있도록 Map 형태로 userId 반환
        return ResponseEntity.ok(Map.of("userId", savedUser.getUserId()));
    }

    /**
     * GET /api/users/{userId}
     * - userId로 사용자 기본 정보를 조회합니다.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserInfo(@PathVariable String userId) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 3단계에서 만든 DTO로 변환하여 반환
            return ResponseEntity.ok(new UserResponseDto(user));
        } else {
            // 사용자를 찾을 수 없는 경우 404 Not Found 반환
            return ResponseEntity.notFound().build();
        }
    }
}