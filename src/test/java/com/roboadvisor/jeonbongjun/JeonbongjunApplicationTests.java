package com.roboadvisor.jeonbongjun;

import com.roboadvisor.jeonbongjun.domain.TestEntity;
import com.roboadvisor.jeonbongjun.domain.TestEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// AssertJ를 사용한 검증 (권장)
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JeonbongjunApplicationTests {

	// 1. 기존 contextLoads() 테스트는 그대로 둡니다.
	@Test
	void contextLoads() {
	}

	// 2. 리포지토리를 주입받습니다.
	@Autowired
	private TestEntityRepository testEntityRepository;

	// 3. DB 저장 및 조회 테스트 메소드를 추가합니다.
	@Test
	void databaseSaveAndFindTest() {
		// given (준비)
		String testName = "테스트데이터";
		testEntityRepository.save(new TestEntity(testName));

		// when (실행)
		TestEntity foundEntity = testEntityRepository.findAll().get(0);

		// then (검증)
		assertThat(foundEntity).isNotNull();
		assertThat(foundEntity.getName()).isEqualTo(testName);

		System.out.println("조회된 데이터 ID: " + foundEntity.getId());
		System.out.println("조회된 데이터 이름: " + foundEntity.getName());
	}
}