package com.roboadvisor.jeonbongjun.service;

import com.roboadvisor.jeonbongjun.dto.WatchlistDto;
import com.roboadvisor.jeonbongjun.entity.User;
import com.roboadvisor.jeonbongjun.entity.UserWatchlist;
import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.global.exception.NotFoundException;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import com.roboadvisor.jeonbongjun.repository.UserRepository;
import com.roboadvisor.jeonbongjun.repository.UserWatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final UserWatchlistRepository userWatchlistRepository;

    public List<WatchlistDto.ItemResponse> list(String userId) {
        var fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return userWatchlistRepository.findByUser_UserId(userId)
                .stream()
                .map(w -> new WatchlistDto.ItemResponse(
                        w.getStock().getStockId(),
                        w.getStock().getTickerSymbol(),
                        w.getStock().getStockName(),
                        w.getStock().getMarket(),
                        w.getAddedAt() == null ? null : w.getAddedAt().format(fmt)
                ))
                .toList();
    }

    @Transactional
    public void add(String userId, String stockId) {
        // 이미 있으면 무시(idempotent)
        if (userWatchlistRepository.existsByUser_UserIdAndStock_StockId(userId, stockId)) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new NotFoundException("Stock not found: " + stockId));

        UserWatchlist wl = UserWatchlist.builder()
                .user(user)
                .stock(stock)
                .build();
        userWatchlistRepository.save(wl);
    }

    @Transactional
    public void remove(String userId, String stockId) {
        userWatchlistRepository.deleteByUser_UserIdAndStock_StockId(userId, stockId);
    }
}