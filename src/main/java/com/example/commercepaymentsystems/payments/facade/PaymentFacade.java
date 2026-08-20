package com.example.commercepaymentsystems.payments.facade;

import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.entity.OrderStatus;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFacade {
    private final PaymentService paymentService;
    private final PaymentCommandService commandService;

    public PaymentConfirmResponse paymentConfirm(Long userId, PaymentConfirmRequest confirmRequest) {
        Payment payment = paymentService.findByOrderIdWithOrder(confirmRequest.orderId());
        Order order = payment.getOrder();

        //결제 중복 확인
        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
            throw new RuntimeException("이미 처리된 결제입니다.");
        }

        //주문자와 사용자 일치 확인
        if (!order.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("주문자와 사용자가 일치하지 않음");
        }

        //주문 상태 전이 가능 여부 확인
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new RuntimeException("주문 상태가 유효하지 않습니다.");
        }

        //결제 금액과 주문 금액 검증
        if (!confirmRequest.paymentPrice().equals(payment.getFinalPrice())) {
            throw new RuntimeException("금액이 다릅니다.");
        }

        //결제 실패 시 payment 상태를 FAILED, order 상태를 CANCELLED로
        //상품 재고 전량 복구
        if(confirmRequest.result().equals("FAIL")) {
            commandService.failPaymentAndOrder(order.getId());

            throw new RuntimeException("결제 실패");
        }

        return commandService.approvePaymentAndOrder(order.getId());
    }
}
