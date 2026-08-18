package com.example.commercepaymentsystems.domain.payment.service;

import com.example.commercepaymentsystems.customers.entity.Customer;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.payments.dto.PaymentResponse;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.repository.PaymentRepository;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTests {
    @Mock
    private PaymentRepository repo;
    @InjectMocks
    private PaymentService service;

    @Test
    @DisplayName("결제 단건 조회 테스트 - 성공")
    void find_payment_by_id_success() {
        //given
        Customer customer = new Customer(
                "email@email.com",
                "password",
                "name"
        );
        ReflectionTestUtils.setField(customer, "id", 1L);
        Payment payment = new Payment(
            10000L,
                PaymentStatus.IN_PROGRESS,
                new Order(
                        customer
                )
        );
        ReflectionTestUtils.setField(payment, "id", 1L);
        given(repo.findByIdAndCustomerId(anyLong(), anyLong())).willReturn(Optional.of(payment));

        //when
        PaymentResponse res = service.getPayment(1L, 1L);

        //then
        verify(repo).findByIdAndCustomerId(anyLong(), anyLong());
        assertEquals(10000L, res.totalPrice());
    }

    @Test
    @DisplayName("결제 단건 조회 테스트 - 찾을 수 없음")
    void find_payment_by_id_failure() {
        //given
        given(repo.findByIdAndCustomerId(anyLong(), anyLong())).willReturn(Optional.empty());

        //when&then
        assertThrows(RuntimeException.class, () -> service.getPayment(1L, 1L));
    }
}
