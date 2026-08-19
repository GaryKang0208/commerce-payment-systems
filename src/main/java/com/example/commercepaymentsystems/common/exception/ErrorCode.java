package com.example.commercepaymentsystems.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Auth (AUTH_xxx)
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH_001", "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_004", "인증이 필요합니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.CONFLICT, "AUTH_005", "현재 비밀번호와 동일하지 않습니다"),

    // Customer (CUSTOMER_xxx)
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "CUSTOMER_001", "회원을 찾을 수 없습니다."),

    // Common (COMMON_xxx)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
