package com.example.commercepaymentsystems.customers.controller.auth;
import com.example.commercepaymentsystems.common.ApiResponse;
import com.example.commercepaymentsystems.customers.dto.auth.LoginRequest;
import com.example.commercepaymentsystems.customers.dto.auth.SignupRequest;
import com.example.commercepaymentsystems.customers.dto.auth.TokenResponse;
import com.example.commercepaymentsystems.customers.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody SignupRequest request
    ) {
        authService.signUp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("회원가입이 완료되었습니다", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse token =
                authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("로그인에 성공했습니다", token)
        );
    }
}
