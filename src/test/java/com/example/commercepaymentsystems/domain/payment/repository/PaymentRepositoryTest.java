package com.example.commercepaymentsystems.domain.payment.repository;

import com.example.commercepaymentsystems.common.config.JpaAuditingConfig;
import com.example.commercepaymentsystems.customers.entity.Customers;
import com.example.commercepaymentsystems.customers.repository.CustomersRepository;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.repository.OrderRepository;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.repository.PaymentRepository;
import com.example.commercepaymentsystems.support.MySQLSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import({MySQLSupport.class, JpaAuditingConfig.class})
@ActiveProfiles("test")
public class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomersRepository customerRepository;

    private Order order;
    private Long customerId;

    @BeforeEach
    void setup() {
        Customers customer = new Customers(
                "email@email.com",
                "password",
                "name",
                "000-0000-0000"
        );
        order = new Order(
                customer,
                "order_num",
                10000L
        );

        customerId = customerRepository.save(customer).getId();
        orderRepository.save(order);
    }

    @Test
    @DisplayName("findByOrderIdWithOrder 메서드 테스트")
    void findByOrderIdWithOrder_test() {
        //given
        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );
        paymentRepository.save(payment);

        //when
        Payment found = paymentRepository.findByOrderIdWithOrder(1L)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        //then
        assertEquals("name", found.getOrder().getCustomer().getName());
    }

    @Test
    @DisplayName("findByIdAndCustomerId 메서드 테스트")
    void findByIdAndCustomerId_test() {
        //given
        Payment payment = new Payment(
                10000L,
                PaymentStatus.IN_PROGRESS,
                order
        );
        paymentRepository.save(payment);

        //when
        Payment found = paymentRepository.findByIdAndCustomerId(1L, customerId)
                .orElseThrow(() -> new RuntimeException("Payment not found") );

        //then
        assertEquals(10000L, found.getFinalPrice());
    }
}
