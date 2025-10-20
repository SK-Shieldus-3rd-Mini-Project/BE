package com.roboadvisor.jeonbongjun.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER")
public class User {

    @Id
    @Column(name = "user_id")
    private String userId; // PK, VARCHAR

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // User가 삭제되면 관련 포트폴리오도 삭제 (Cascade)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPortfolio> portfolioList = new ArrayList<>();

    // User가 삭제되면 관련 관심 목록도 삭제 (Cascade)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserWatchlist> watchList = new ArrayList<>();

    // User가 삭제되면 관련 채팅 세션도 삭제 (Cascade)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatSession> chatSessionList = new ArrayList<>();
}