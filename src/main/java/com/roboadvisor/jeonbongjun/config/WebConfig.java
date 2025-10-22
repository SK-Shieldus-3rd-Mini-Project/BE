package com.roboadvisor.jeonbongjun.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // /api/ 하위 모든 경로
                .allowedOrigins("http://localhost:5173") // React 개발 서버 주소
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true) // ★ 세션 쿠키를 주고받기 위해 필수
                .maxAge(3600);
    }

    // --- AI 서비스 통신용 WebClient 빈 추가 ---
    @Bean
    public WebClient aiWebClient(WebClient.Builder webClientBuilder,
                                 @Value("${ai.api.base-url}") String aiApiBaseUrl) { // application.properties 등에서 AI 서버 주소 주입
        return webClientBuilder.baseUrl(aiApiBaseUrl).build();
    }
}