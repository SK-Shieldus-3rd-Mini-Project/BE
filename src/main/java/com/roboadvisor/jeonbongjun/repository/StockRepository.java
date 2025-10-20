package com.roboadvisor.jeonbongjun.repository;

import com.roboadvisor.jeonbongjun.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockRepository extends JpaRepository<Stock, String> {}

