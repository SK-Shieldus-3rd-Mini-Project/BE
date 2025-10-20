package com.roboadvisor.jeonbongjun.watchlist.repository;

import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.entity.User;
import com.roboadvisor.jeonbongjun.entity.UserWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWatchlistRepository extends JpaRepository<UserWatchlist, Integer> {
    List<UserWatchlist> findByUser_UserId(String userId);
    boolean existsByUser_UserIdAndStock_StockId(String userId, String stockId);
    void deleteByUser_UserIdAndStock_StockId(String userId, String stockId);
    boolean existsByUserAndStock(User user, Stock stock);
}

