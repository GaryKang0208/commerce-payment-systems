package com.example.commercepaymentsystems.customers.service.customers;
import com.example.commercepaymentsystems.common.config.PasswordEncoder;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.customers.dto.customers.ChangePasswordRequest;
import com.example.commercepaymentsystems.customers.dto.customers.ProfileResponse;
import com.example.commercepaymentsystems.customers.dto.customers.UpdateProfileRequest;
import com.example.commercepaymentsystems.customers.entity.Customers;
import com.example.commercepaymentsystems.customers.repository.CustomersRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomersService {
    private final CustomersRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long customerId) {
        Customers customer = repository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));
        return new ProfileResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getName(),
                customer.getPhoneNumber(),
                customer.getPoint()
        );
    }

    public void updateProfile(Long customerId,
            UpdateProfileRequest request
    ) {
        Customers customer = repository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        repository.findByEmail(request.email()).ifPresent(existingCustomer -> {
                    if (!existingCustomer.getId().equals(customerId)) {
                        throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);}
        });
        customer.updateInfo(
                request.email(),
                request.name(),
                request.phoneNumber()
        );
    }

    public void changePassword(
            Long customerId,
            ChangePasswordRequest request
    ) {
        Customers customer = repository.findById(customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND));

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), customer.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CURRENT_PASSWORD);
        }
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        customer.changePassword(encodedPassword);
    }


}
