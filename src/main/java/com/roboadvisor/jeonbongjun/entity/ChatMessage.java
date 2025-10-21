package com.roboadvisor.jeonbongjun.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CHAT_MESSAGE")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId; // PK, INT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ChatSession chatSession;

    @Column(name = "sender")
    private String sender; // "USER" 또는 "AI"

    @Lob // TEXT 타입 매핑
    @Column(name = "content")
    private String content;

    @CreationTimestamp
    @Column(name = "timestamp", updatable = false)
    private LocalDateTime timestamp;

    // AI 응답 상세 정보 (1:1 관계)
    // ChatMessage가 삭제되면 AiResponseDetail도 삭제됨
    @OneToOne(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private AiResponseDetail aiResponseDetail;

    //== 연관관계 편의 메서드 ==//
    // (AI 응답 메시지일 경우) 상세 정보를 설정할 때 양방향 관계를 세팅
    public void setAiResponseDetail(AiResponseDetail aiResponseDetail) {
        this.aiResponseDetail = aiResponseDetail;
        if (aiResponseDetail != null) {
            aiResponseDetail.setChatMessage(this);
        }
    }

}