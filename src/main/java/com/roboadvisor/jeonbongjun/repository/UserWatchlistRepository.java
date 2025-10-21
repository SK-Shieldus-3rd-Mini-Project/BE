package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.entity.User;
import com.roboadvisor.jeonbongjun.entity.UserWatchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Query import 추가
import org.springframework.data.repository.query.Param; // Param import 추가
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWatchlistRepository extends JpaRepository<UserWatchlist, Integer> {

    // ★ N+1 해결: 관심 목록 조회 시 Stock 정보를 JOIN FETCH로 같이 가져온다
    @Query("SELECT uw FROM UserWatchlist uw JOIN FETCH uw.stock s WHERE uw.user.userId = :userId")
    List<UserWatchlist> findByUserIdWithStock(@Param("userId") String userId);

    boolean existsByUser_UserIdAndStock_StockId(String userId, String stockId);
    void deleteByUser_UserIdAndStock_StockId(String userId, String stockId);
    boolean existsByUserAndStock(User user, Stock stock);
}

