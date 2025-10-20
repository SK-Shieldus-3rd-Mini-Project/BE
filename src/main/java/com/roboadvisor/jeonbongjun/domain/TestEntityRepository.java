package com.roboadvisor.jeonbongjun.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
    // JpaRepository<엔티티 클래스, ID 필드 타입>
    // 기본적인 save(), findById(), findAll(), delete() 등의 메소드가 자동으로 제공됩니다.
}