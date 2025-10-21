package com.roboadvisor.jeonbongjun.global.exception;

// import org.slf4j.Logger; // Logger import 추가
// import org.slf4j.LoggerFactory; // LoggerFactory import 추가
import org.springframework.http.HttpStatus; // HttpStatus import 추가
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError; // FieldError import 추가
import org.springframework.web.bind.MethodArgumentNotValidException; // MethodArgumentNotValidException import 추가
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap; // HashMap import 추가
import java.util.Map; // Map import 추가


@RestControllerAdvice
public class GlobalExceptionHandler {

    // private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class); // Logger 추가

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        // log.error("CustomException occurred: {} - {}", errorCode, e.getMessage()); // 로그 추가
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getHttpStatus().value())
                        .error(errorCode.getHttpStatus().name())
                        .message(errorCode.getMessage())
                        .build());
    }

    // @Valid 유효성 검사 실패 시 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            // log.warn("Validation error: Field '{}' - {}", fieldName, errorMessage); // 로그 추가
        });
        return ResponseEntity.badRequest().body(errors);
    }


    // 그 외 예상치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        // log.error("Unhandled Exception occurred: ", e); // 상세 스택 트레이스 로깅
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.name())
                        .message("서버 내부 오류가 발생했습니다.")
                        .build());
    }

}