package com.example.commercepaymentsystems.domain.payment.repository;

import com.example.commercepaymentsystems.customers.CustomerRepository;
import com.example.commercepaymentsystems.customers.entity.Customer;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.repository.OrderRepository;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.repository.PaymentRepository;
import com.example.commercepaymentsystems.support.MySQLSupport;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(MySQLSupport.class)
@ActiveProfiles("test")
public class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Order order;

    @BeforeEach
    void setup() {
        Customer customer = new Customer(
                "email@email.com",
                "password",
                "name"
        );
        order = new Order(customer);

        customerRepository.save(customer);
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
}
