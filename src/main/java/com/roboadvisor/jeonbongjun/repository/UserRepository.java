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


    @Modifying
    @Query("DELETE FROM User u WHERE u.lastActivityAt < :cutoffDate")
    int deleteInactiveUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
}