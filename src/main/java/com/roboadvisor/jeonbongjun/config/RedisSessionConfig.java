package com.roboadvisor.jeonbongjun.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession // Redis를 HTTP 세션 저장소로 사용하도록 활성화
public class RedisSessionConfig {
    // 이 클래스는 활성화 어노테이션 외에 별도 설정이 필요 없을 수 있습니다.
}