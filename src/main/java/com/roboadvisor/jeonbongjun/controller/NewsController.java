package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.NewsDto;
import com.roboadvisor.jeonbongjun.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @GetMapping("/latest")
    public ResponseEntity<List<NewsDto>> getLatestNews() {
        List<NewsDto> newsList = newsService.getLatestMarketNews();
        return ResponseEntity.ok(newsList);
    }
}