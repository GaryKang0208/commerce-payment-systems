package com.example.commercepaymentsystems.payments.facade;

import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.entity.OrderStatus;
import com.example.commercepaymentsystems.payments.dto.PaymentCancelRequest;
import com.example.commercepaymentsystems.payments.dto.PaymentCancelResponse;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.port.PaymentGateway;
import com.example.commercepaymentsystems.payments.port.PaymentGatewayResponse;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import com.example.commercepaymentsystems.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFacade {
    private final PaymentService paymentService;
    private final PaymentCommandService commandService;
    private final PaymentGateway paymentGateway;
    private final PointService pointService;

    public PaymentConfirmResponse paymentConfirm(Long userId, PaymentConfirmRequest confirmRequest) {
        Payment payment = paymentService.findByOrderIdWithOrder(confirmRequest.orderId());
        Order order = payment.getOrder();
        Long customerId = order.getCustomer().getId();

        //주문자와 사용자 일치 확인
        if (!customerId.equals(userId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        //결제 중복 확인
        if (payment.getStatus() != PaymentStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        //주문 상태 전이 가능 여부 확인
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        //포인트 사용 가능 여부 확인
        if (pointService.getBalance(customerId).Balance() > payment.getPointUsed()) {
           throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }

        //PG 사에서 실결제 정보 확인
        PaymentGatewayResponse paymentInfo = paymentGateway.getPayment(payment.getPortoneId());

        //결제 금액과 주문 금액 검증
        if (!confirmRequest.paymentPrice().equals(paymentInfo.totalAmount())) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        //결제 실패 시 payment 상태를 FAILED, order 상태를 CANCELLED로
        //상품 재고 전량 복구
        if(confirmRequest.result().equals("FAIL")) {
            commandService.failPaymentAndOrder(order.getId());

            throw new BusinessException(ErrorCode.PG_FAILURE);
        }

        return commandService.approvePaymentAndOrder(order.getId());
    }

    public PaymentCancelResponse paymentCancel(Long customerId, Long paymentId, PaymentCancelRequest request) {
        String cancelReason = (request != null && request.reason() != null)
                ? request.reason() : "사용자 요청 취소";

        log.info("PaymentId: {}", paymentId);

        Payment payment = paymentService.findByIdWithOrder(paymentId);
        if (!payment.getOrder().getCustomer().getId().equals(customerId)) {
            throw new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        }

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        PaymentCancelResponse response = commandService.cancelPaymentAndOrder(paymentId);

        try {
            paymentGateway.cancelPayment(payment.getPortoneId(), null, cancelReason);
        } catch (Exception e) {
            log.error("PG 결제 취소 실패 : DB 커밋됨, 수동 처리 필요 paymentId={}", response.portoneId(), e);
        }

        return response;
    }
}
