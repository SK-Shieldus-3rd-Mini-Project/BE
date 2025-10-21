package com.roboadvisor.jeonbongjun.service;

import com.roboadvisor.jeonbongjun.dto.NewsDto;
import com.roboadvisor.jeonbongjun.dto.deepsearch.DeepSearchArticle;
import com.roboadvisor.jeonbongjun.dto.deepsearch.DeepSearchResponse;
// 1. HighlightDto를 import 해야 합니다. (이 파일은 아래 '주의사항' 참고)
import com.roboadvisor.jeonbongjun.dto.deepsearch.HighlightDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
     * DeepSearch API를 호출하여 최신 경제/기술 뉴스를 가져옵니다.
     */
    public List<NewsDto> getLatestMarketNews() {

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

        // API 호출 부분은 기존 코드와 동일합니다.
        DeepSearchResponse response = deepSearchWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/articles") // 키워드 검색
                        .queryParam("api_key", this.apiKey)
                        .queryParam("keyword", "경제 OR AI OR 투자 OR 금리")
                        .queryParam("date_from", today)
                        .queryParam("date_to", today)
                        .queryParam("page_size", 5)
                        .queryParam("order", "published_at") // 최신순
                        .queryParam("highlight", "unified") // 요약본 포함
                        .build())
                .retrieve()
                .bodyToMono(DeepSearchResponse.class)
                .block();

        if (response == null || response.getData() == null) {
            return Collections.emptyList();
        }

        return response.getData().stream()
                .map(this::mapArticleToNewsDto) // 2. 이 매핑 로직이 수정되었습니다.
                .collect(Collectors.toList());
    }

    /**
     * DeepSearch 응답(DeepSearchArticle)을 프론트엔드 DTO(NewsDto)로 변환합니다.
     * Postman 응답을 기준으로 필드명을 매핑합니다.
     */
    private NewsDto mapArticleToNewsDto(DeepSearchArticle article) {

        // --- [매핑 로직 수정] ---

        // 1. 카드 요약 (Postman의 highlight.content 배열의 첫 번째 값)
        String summaryHighlight = null;
        // (DeepSearchArticle에 HighlightDto highlight 필드가 있어야 함)
        if (article.getHighlight() != null && article.getHighlight().getContent() != null && !article.getHighlight().getContent().isEmpty()) {
            summaryHighlight = article.getHighlight().getContent().get(0);
        }

        // 2. 모달 본문 (Postman의 summary 필드)
        // (DeepSearchArticle에 String summary 필드가 있어야 함)
        String fullContent = article.getSummary();

        // 3. 원문 URL (Postman의 content_url 필드)
        // (DeepSearchArticle에 String contentUrl 필드가 있어야 함)
        String url = article.getContentUrl();

        return NewsDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .press(article.getPublisher())
                .summary(summaryHighlight) // 카드 요약
                .fullContent(fullContent) // 모달 본문 (API의 summary를 사용)
                .url(url) // 원문 링크
                .build();
    }
}