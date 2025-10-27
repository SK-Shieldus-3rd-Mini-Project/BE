package com.roboadvisor.jeonbongjun.service;

import com.roboadvisor.jeonbongjun.dto.NewsDto;
import com.roboadvisor.jeonbongjun.dto.deepsearch.DeepSearchArticle;
import com.roboadvisor.jeonbongjun.dto.deepsearch.DeepSearchResponse;
import com.roboadvisor.jeonbongjun.dto.deepsearch.HighlightDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono; // Mono 임포트

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsService {

    private final WebClient deepSearchWebClient;
    private final String apiKey;

    public NewsService(WebClient.Builder webClientBuilder,
                       @Value("${deepsearch.api.base-url}") String baseUrl,
                       @Value("${deepsearch.api.key}") String apiKey) {

        this.deepSearchWebClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    /**
     * [신규] 특정 키워드(종목명)로 뉴스를 검색합니다. (StockDetailService용)
     * 리액티브(Mono) 방식으로 반환합니다.
     */
    public Mono<List<NewsDto>> searchNews(String keyword) {
        // 최근 3일간의 뉴스를 검색
        String dateFrom = LocalDate.now().minusDays(3).format(DateTimeFormatter.ISO_LOCAL_DATE);
        String dateTo = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        return deepSearchWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/articles")
                        .queryParam("api_key", this.apiKey)
                        .queryParam("keyword", keyword) // 파라미터로 받은 키워드 사용
                        .queryParam("date_from", dateFrom)
                        .queryParam("date_to", dateTo)
                        .queryParam("page_size", 10) // 상세 페이지에서는 10개만
                        .queryParam("order", "published_at")
                        .queryParam("highlight", "unified")
                        .build())
                .retrieve()
                .bodyToMono(DeepSearchResponse.class)
                .map(response -> {
                    if (response == null || response.getData() == null) {
                        return Collections.<NewsDto>emptyList();
                    }
                    return response.getData().stream()
                            .map(this::mapArticleToNewsDto)
                            .collect(Collectors.toList());
                })
                .onErrorReturn(Collections.emptyList()); // 에러 발생 시 빈 리스트 반환
    }


    /**
     * DeepSearch API를 호출하여 최신 경제/기술 뉴스를 가져옵니다. (기존 메서드)
     * (이 메서드는 block()을 사용하므로 StockDetailService에서 사용 X)
     */
    public List<NewsDto> getLatestMarketNews() {

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        DeepSearchResponse response = deepSearchWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/articles")
                        .queryParam("api_key", this.apiKey)
                        .queryParam("keyword", "경제 OR AI OR 투자 OR 금리")
                        .queryParam("date_from", today)
                        .queryParam("date_to", today)
                        .queryParam("page_size", 20)
                        .queryParam("order", "published_at")
                        .queryParam("highlight", "unified")
                        .build())
                .retrieve()
                .bodyToMono(DeepSearchResponse.class)
                .block(); // 블로킹 호출

        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }

        return response.getData().stream()
                .map(this::mapArticleToNewsDto)
                .collect(Collectors.toList());
    }

    /**
     * DeepSearch 응답(DeepSearchArticle)을 프론트엔드 DTO(NewsDto)로 변환합니다.
     * (기존 코드와 동일)
     */
    private NewsDto mapArticleToNewsDto(DeepSearchArticle article) {

        String summaryHighlight = null;
        if (article.getHighlight() != null && article.getHighlight().getContent() != null && !article.getHighlight().getContent().isEmpty()) {
            summaryHighlight = article.getHighlight().getContent().get(0);
        }

        String fullContent = article.getSummary();
        String url = article.getContentUrl();

        return NewsDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .press(article.getPublisher())
                .summary(summaryHighlight)
                .fullContent(fullContent)
                .url(url)
                .build();
    }
}