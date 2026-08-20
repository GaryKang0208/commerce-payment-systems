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
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),

    //products (PRODUCT_xxx)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "저장되지 않은 상품입니다"),
    INVALID_PAGE(HttpStatus.BAD_REQUEST, "PRODUCT_002", "페이지 번호는 0 이상이어야 합니다"),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "PRODUCT_003", "페이지 크기는 1 이상 100 이하이어야 합니다"),
    INVALID_MINIMUM_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT_004", "최소 가격은 0이상이어야 합니다."),
    INVALID_MAXIMUM_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT_005", "최대값 가격은 0이상이어야 합니다."),
    INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "PRODUCT_006", "최소 가격은 최대 가격보다 클수 없다"),

    // Order (ORDER_xxx)
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_001", "장바구니 상품을 찾을 수 없습니다."),
    CART_ITEM_FORBIDDEN(HttpStatus.FORBIDDEN, "ORDER_002", "본인의 장바구니 상품만 조회할 수 있습니다."),
    ORDER_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_003", "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "ORDER_004", "상품 재고가 부족합니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_005", "주문을 찾을 수 없습니다."),
    ORDER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ORDER_006", "본인의 주문만 조회할 수 있습니다."),
    ORDER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "ORDER_007", "인증이 필요합니다."),
    INVALID_ORDER_STATUS(HttpStatus.CONFLICT, "ORDER_008", "변경할 수 없는 주문 상태입니다."),
    //payments (PAYMENT_xxx)
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_001", "결제 정보를 찾을 수 없습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "PAYMENT_002", "유효하지 않은 상태입니다."),
    ALREADY_PROCESSED_PAYMENT(HttpStatus.BAD_REQUEST, "PAYMENT_003", "이미 처리된 결제입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_004", "결제 금액 정보가 일치하지 않습니다."),
    PG_PAYMENT_FAILURE(HttpStatus.BAD_REQUEST, "PAYMENT_005", "PG 결제 시도 실패"),
  
    //cart (CART_xxx)
    CART_EMPTY(HttpStatus.BAD_REQUEST, "CART_001", "장바구니가 비어있습니다." ),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "CART_003", "수량은 1 이상이어야 합니다."),
    STOCK_EXCEEDED(HttpStatus.CONFLICT,"CART_004", "재고가 부족하여 담을 수 없습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
