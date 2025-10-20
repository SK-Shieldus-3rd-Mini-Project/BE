package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.UserPortfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserPortfolioRepository extends JpaRepository<UserPortfolio, Integer> {

    // ★ N+1 해결: 포트폴리오 조회 시 Stock 정보를 JOIN FETCH로 같이 가져온다
    @Query("SELECT up FROM UserPortfolio up " +
           "JOIN FETCH up.stock s " +
           "WHERE up.user.userId = :userId")
    List<UserPortfolio> findAllByUserIdWithStock(@Param("userId") String userId);

    Optional<UserPortfolio> findByUserUserIdAndStockStockId(String userId, String stockId);

    boolean existsByUserUserIdAndStockStockId(String userId, String stockId);
}