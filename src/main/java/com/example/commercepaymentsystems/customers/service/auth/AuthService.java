package com.example.commercepaymentsystems.customers.service.auth;
import com.example.commercepaymentsystems.common.config.PasswordEncoder;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.common.jwt.JwtTokenProvider;
import com.example.commercepaymentsystems.customers.dto.auth.LoginRequest;
import com.example.commercepaymentsystems.customers.dto.auth.SignupRequest;
import com.example.commercepaymentsystems.customers.dto.auth.TokenResponse;
import com.example.commercepaymentsystems.customers.entity.Customers;
import com.example.commercepaymentsystems.customers.repository.CustomersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final CustomersRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signUp(SignupRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new BusinessException(
                    ErrorCode.DUPLICATE_EMAIL
            );
        }Customers customer = new Customers(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.name(),
                request.phoneNumber()
        );
        repository.save(customer);
    }

    public TokenResponse login(LoginRequest request) {
        Customers customer =
                repository.findByEmail(request.email()).orElseThrow(() -> new BusinessException(
                                        ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), customer.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        String token = jwtTokenProvider.createToken(customer.getId());
        return new TokenResponse(token);
    }
}
