package com.roboadvisor.jeonbongjun.service;

import com.roboadvisor.jeonbongjun.dto.SessionResponseDto;
import com.roboadvisor.jeonbongjun.entity.User;
import com.roboadvisor.jeonbongjun.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UserRepository userRepository; //

    @Transactional
    public SessionResponseDto initializeSession(HttpSession session) {

        String userId = (String) session.getAttribute("USER_ID");

        if (userId != null) {
            Optional<User> userOptional = userRepository.findById(userId);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                user.setLastActivityAt(LocalDateTime.now());
                userRepository.save(user);

                return new SessionResponseDto(user.getUserId(), false);
            }
        }


        User newUser = User.builder().build();

        User savedUser = userRepository.save(newUser);

        String newUserId = savedUser.getUserId();
        session.setAttribute("USER_ID", newUserId);

        return new SessionResponseDto(newUserId, true);
    }
}