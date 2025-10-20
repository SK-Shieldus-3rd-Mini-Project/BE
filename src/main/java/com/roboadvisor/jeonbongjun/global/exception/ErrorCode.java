package com.roboadvisor.jeonbongjun.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 종목을 찾을 수 없습니다."),
    PORTFOLIO_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 보유 종목을 찾을 수 없습니다."),

    // 409 CONFLICT
    ALREADY_IN_PORTFOLIO(HttpStatus.CONFLICT, "이미 포트폴리오에 등록된 종목입니다.");
    
    private final HttpStatus httpStatus;
    private final String message;
}