package com.roboadvisor.jeonbongjun.controller;

import com.roboadvisor.jeonbongjun.dto.WatchlistDto;
import com.roboadvisor.jeonbongjun.service.WatchlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    // 관심 종목 조회
    @GetMapping
    public ResponseEntity<List<WatchlistDto.ItemResponse>> list(@PathVariable String userId) {
        return ResponseEntity.ok(watchlistService.list(userId));
    }

    // 관심 종목 추가
    @PostMapping
    public ResponseEntity<Void> add(@PathVariable String userId,
                                    @Valid @RequestBody WatchlistDto.AddRequest req) {
        watchlistService.add(userId, req.stockId());
        return ResponseEntity.ok().build();
    }

    // 관심 종목 삭제
    @DeleteMapping("/{stockId}")
    public ResponseEntity<Void> remove(@PathVariable String userId,
                                       @PathVariable String stockId) {
        watchlistService.remove(userId, stockId);
        return ResponseEntity.noContent().build();
    }
}