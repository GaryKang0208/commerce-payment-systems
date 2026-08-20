package com.example.commercepaymentsystems.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


/**
 * 에러 코드 작성 규칙
 * 에러 코드(ErrorCode.code)는 도메인_번호로 작성합니다.
 * 어느 도메인에서 발생했는지만 신경써주시면 됩니다.
 * _ 뒤의 번호는 임의의 순서대로 작성해주셔도 무관합니다.
**/
@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    //common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
