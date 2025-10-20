package com.roboadvisor.jeonbongjun.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor // JPA는 기본 생성자가 필요합니다.
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // MariaDB/MySQL은 IDENTITY 전략을 사용합니다.
    private Long id;

    private String name;

    public TestEntity(String name) {
        this.name = name;
    }
}