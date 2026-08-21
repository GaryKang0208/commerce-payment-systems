package com.example.commercepaymentsystems.payments.facade;

import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
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
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        //주문자와 사용자 일치 확인
        if (!order.getCustomer().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        //주문 상태 전이 가능 여부 확인
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        //결제 금액과 주문 금액 검증
        if (!confirmRequest.paymentPrice().equals(payment.getFinalPrice())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        //결제 실패 시 payment 상태를 FAILED, order 상태를 CANCELLED로
        //상품 재고 전량 복구
        if(confirmRequest.result().equals("FAIL")) {
            commandService.failPaymentAndOrder(order.getId());

            throw new BusinessException(ErrorCode.PG_PAYMENT_FAILURE);
        }

        return commandService.approvePaymentAndOrder(order.getId());
    }
}
