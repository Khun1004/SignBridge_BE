package com.signbridge;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.signbridge.entity.User;
import com.signbridge.repository.UserRepository;

@SpringBootTest
class SignBridgeApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
		// 기본 컨텍스트 로드 테스트
	}

	@Test
	void testDatabaseConnection() {
		// 1. 테스트 데이터 생성
		User user = User.builder()
				.email("test@example.com")
				.password("password123")
				.orgType("personal")
				.name("테스터")
				.build();

		// 2. 저장
		User savedUser = userRepository.save(user);

		// 3. 검증
		assertThat(savedUser.getId()).isNotNull();
		assertThat(savedUser.getEmail()).isEqualTo("test@example.com");

		System.out.println("데이터베이스 연결 및 저장 테스트 성공! ID: " + savedUser.getId());
	}
}