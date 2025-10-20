package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // 임포트 추가

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 특정 시간 이전에 마지막으로 활동한 사용자들을 모두 삭제합니다.
     * @Modifying: DML(DELETE, UPDATE) 쿼리임을 알림
     * @Transactional: 이 메서드는 트랜잭션 내부에서 실행되어야 함 (서비스단에서 추가)
     */
    @Modifying
    @Query("DELETE FROM User u WHERE u.lastActivityAt < :cutoffDate")
    int deleteInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
}