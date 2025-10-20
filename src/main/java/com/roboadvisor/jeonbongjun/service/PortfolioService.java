package com.roboadvisor.jeonbongjun.service;

import com.roboadvisor.jeonbongjun.entity.Stock;
import com.roboadvisor.jeonbongjun.entity.User;
import com.roboadvisor.jeonbongjun.entity.UserPortfolio;
import com.roboadvisor.jeonbongjun.dto.PortfolioDto;
import com.roboadvisor.jeonbongjun.global.exception.CustomException;
import com.roboadvisor.jeonbongjun.global.exception.ErrorCode;
import com.roboadvisor.jeonbongjun.repository.StockRepository;
import com.roboadvisor.jeonbongjun.repository.UserRepository;
import com.roboadvisor.jeonbongjun.repository.UserPortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    // 기존 파일들을 수정하지 않기 위해 Repository를 직접 주입받음
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final UserPortfolioRepository portfolioRepository;

    // --- (서비스 내부 헬퍼 메서드) ---
    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
    
    private Stock findStockById(String stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new CustomException(ErrorCode.STOCK_NOT_FOUND));
    }
    // ---

    /**
     * 1. 보유 종목 추가 (POST)
     */
    @Transactional
    public PortfolioDto.Response addStock(String userId, PortfolioDto.AddRequest request) {
        User user = findUserById(userId);
        Stock stock = findStockById(request.getStockId());

        if (portfolioRepository.existsByUserUserIdAndStockStockId(userId, request.getStockId())) {
            throw new CustomException(ErrorCode.ALREADY_IN_PORTFOLIO);
        }

        UserPortfolio portfolio = UserPortfolio.builder()
                .user(user)
                .stock(stock)
                .quantity(request.getQuantity())
                .avgPurchasePrice(request.getAvgPurchasePrice())
                .build();

        portfolioRepository.save(portfolio);
        return PortfolioDto.Response.fromEntity(portfolio);
    }

    /**
     * 2. 보유 종목 조회 (GET) - N+1 해결
     */
    public List<PortfolioDto.Response> getPortfolio(String userId) {
        // N+1 해결 쿼리(Fetch Join) 호출
        List<UserPortfolio> portfolioList = portfolioRepository.findAllByUserIdWithStock(userId);
        
        return portfolioList.stream()
                .map(PortfolioDto.Response::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 3. 보유 종목 수정 (PUT)
     * (기존 UserPortfolio 엔티티 수정 없이, Delete & Create 방식으로 구현)
     */
    @Transactional
    public PortfolioDto.Response updateStock(String userId, String stockId, PortfolioDto.UpdateRequest request) {
        User user = findUserById(userId);
        Stock stock = findStockById(stockId);

        // 1. 기존 아이템 조회
        UserPortfolio existingItem = portfolioRepository.findByUserUserIdAndStockStockId(userId, stockId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_ITEM_NOT_FOUND));

        // 2. 기존 아이템 삭제
        portfolioRepository.delete(existingItem);
        
        // 3. 새 정보로 새 아이템 생성
        UserPortfolio updatedItem = UserPortfolio.builder()
                .user(user)
                .stock(stock)
                .quantity(request.getQuantity()) // 새 값
                .avgPurchasePrice(request.getAvgPurchasePrice()) // 새 값
                .build();
        
        // 4. 새 아이템 저장
        portfolioRepository.save(updatedItem);
        
        // 새 아이템의 DTO 반환 (portfolio_id가 바뀜)
        return PortfolioDto.Response.fromEntity(updatedItem);
    }

    /**
     * 4. 보유 종목 삭제 (DELETE)
     */
    @Transactional
    public void deleteStock(String userId, String stockId) {
        UserPortfolio portfolio = portfolioRepository.findByUserUserIdAndStockStockId(userId, stockId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_ITEM_NOT_FOUND));
        
        portfolioRepository.delete(portfolio);
    }
}