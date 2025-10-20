package com.roboadvisor.jeonbongjun.service;

import com.roboadvisor.jeonbongjun.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserCleanupService {

    private static final Logger log = LoggerFactory.getLogger(UserCleanupService.class);

    @Autowired
    private UserRepository userRepository;

    /**
     * 매일 새벽 4시에 실행 (cron = "초 분 시 일 월 요일")
     * "0 0 4 * * ?" = 매일 04시 00분 00초에 실행
     */
    @Scheduled(cron = "0 0 4 * * ?")
    @Transactional // 3단계의 @Modifying 쿼리를 위해 트랜잭션 필수!
    public void deleteInactiveGuestUsers() {
        // 기준: 90일 동안 활동이 없는 사용자
        LocalDateTime cutoff = LocalDateTime.now().minusDays(90);

        log.info("Starting inactive user cleanup. Deleting users inactive since {}", cutoff);

        int deletedCount = userRepository.deleteInactiveUsers(cutoff);

        log.info("Finished inactive user cleanup. Deleted {} users.", deletedCount);
    }
}