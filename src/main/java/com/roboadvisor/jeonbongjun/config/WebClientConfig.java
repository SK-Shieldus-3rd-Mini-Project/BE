package com.roboadvisor.jeonbongjun.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    // (참고: Alpha Vantage, DeepSearch용 WebClient도 여기에 정의할 수 있습니다)

    /**
     * AI 서버 (Python/FastAPI) 통신용 WebClient Bean
     * @param baseUrl AI 서버의 기본 URL (application.properties에서 주입)
     * @return
     */
    @Bean
    @Qualifier("aiWebClient") // 여러 WebClient Bean을 구분하기 위한 이름
    public WebClient aiWebClient(@Value("${ai.api.base-url}") String baseUrl) {
        
        // 30초 타임아웃 설정
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(30))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl) // application.properties에 설정된 AI 서버 주소
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
