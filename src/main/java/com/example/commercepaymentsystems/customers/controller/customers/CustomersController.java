package com.example.commercepaymentsystems.customers.controller.customers;
import com.example.commercepaymentsystems.common.ApiResponse;
import com.example.commercepaymentsystems.customers.dto.customers.ChangePasswordRequest;
import com.example.commercepaymentsystems.customers.dto.customers.ProfileResponse;
import com.example.commercepaymentsystems.customers.dto.customers.UpdateProfileRequest;
import com.example.commercepaymentsystems.customers.service.customers.CustomersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomersController {
    private final CustomersService customersService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
            @AuthenticationPrincipal Long customerId) {
        ProfileResponse response = customersService.getProfile(customerId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthenticationPrincipal Long customerId,
                                                           @Valid @RequestBody UpdateProfileRequest request) {
        customersService.updateProfile(customerId, request);
        return ResponseEntity.ok(ApiResponse.ok("회원정보 수정이 완료되었습니다.", null));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal Long customerId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        customersService.changePassword(customerId, request);
        return ResponseEntity.ok(ApiResponse.ok("비밀번호 변경이 완료되었습니다.", null));
    }











}
