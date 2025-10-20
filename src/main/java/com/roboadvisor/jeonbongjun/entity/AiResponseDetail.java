package com.roboadvisor.jeonbongjun.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter // 연관관계 편의 메서드(ChatMessage)에서 사용하기 위해 Setter 추가
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "AI_RESPONSE_DETAIL")
public class AiResponseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer detailId; // PK, INT

    // 1:1 관계의 주인(Owning side)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", unique = true) // FK, UNIQUE
    private ChatMessage chatMessage;

    @Lob // TEXT
    @Column(name = "economic_data_used")
    private String economicDataUsed; // JSON or TEXT

    @Lob // TEXT
    @Column(name = "source_citations")
    private String sourceCitations; // JSON

    @Lob // TEXT
    @Column(name = "related_charts_metadata")
    private String relatedChartsMetadata; // JSON

    @Lob // TEXT
    @Column(name = "related_reports")
    private String relatedReports; // JSON

    @Column(name = "rag_model_version")
    private String ragModelVersion;
}