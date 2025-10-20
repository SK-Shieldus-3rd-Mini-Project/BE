package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, String> {
}