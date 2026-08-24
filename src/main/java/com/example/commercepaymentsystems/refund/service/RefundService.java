package com.example.commercepaymentsystems.refund.service;

import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.orders.entity.OrderItem;
import com.example.commercepaymentsystems.orders.repository.OrderItemRepository;
import com.example.commercepaymentsystems.payments.repository.PaymentRepository;
import com.example.commercepaymentsystems.refund.dto.RefundItemRequest;
import com.example.commercepaymentsystems.refund.dto.RefundRequest;
import com.example.commercepaymentsystems.refund.dto.RefundResponse;
import com.example.commercepaymentsystems.refund.entity.Refund;
import com.example.commercepaymentsystems.refund.entity.RefundItem;
import com.example.commercepaymentsystems.refund.repository.RefundItemRepository;
import com.example.commercepaymentsystems.refund.repository.RefundRepository;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.products.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RefundService {

    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;

    private static final Integer ZERO_POINT = 0;

    //부분환불,전체환불 결정하는 로직(request에 item유무로 검증)
    @Transactional
    public RefundResponse refund(
            Long paymentId,
            Long customerId,
            RefundRequest request
    ) {
        // 결제 조회 + 본인 소유 확인
        Payment payment = paymentRepository.findByIdAndCustomerId(paymentId, customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = payment.getOrder();

        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(order.getId());

        // items가 없으면 전액 환불 있으면 부분환불
        if (request.items() == null || request.items().isEmpty()) {
            return refundAll(
                    payment,
                    order,
                    orderItems,
                    request.reason()
            );
        } else {
            return partialRefund(
                    payment,
                    order,
                    orderItems,
                    request
            );
        }
    }

    //전체환불로직
    private RefundResponse refundAll(
            Payment payment,
            Order order,
            List<OrderItem> orderItems,
            String reason
    ) {

        long alreadyRefunded = refundRepository.sumRefundedAmount(payment.getId());

        long refundAmount = payment.getFinalPrice() - alreadyRefunded;

        if (refundAmount <= 0) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_REFUND);
        }

        Refund refund = new Refund(
                payment,
                ZERO_POINT,
                Math.toIntExact(refundAmount),
                reason,
                LocalDateTime.now()
        );

        refundRepository.save(refund);

        for (OrderItem orderItem : orderItems) {

            //환불수량
            int refundedQuantity = refundItemRepository.sumRefundedQuantity(orderItem.getId());

            //잔여수량
            int remainingQuantity = orderItem.getQuantity() - refundedQuantity;

            if (remainingQuantity <= 0) {continue;}

            //수량*제품가격 환불가격 측정
            long itemRefundAmount = orderItem.getProductPrice() * remainingQuantity;

            RefundItem refundItem = new RefundItem(
                    refund,
                    orderItem,
                    remainingQuantity,
                    ZERO_POINT,
                    itemRefundAmount
            );

            refundItemRepository.save(refundItem);

            Product product = orderItem.getProduct();
            product.restoreStock(remainingQuantity);
        }

        // Payment 도메인에서 상태 전이 검증
        payment.markAsCancelled();

        // Order 도메인에서 상태 전이 검증
        order.cancel();

        return new RefundResponse(
                payment.getId(),
                order.getId(),
                order.getStatus().name(),
                payment.getStatus().name(),
                refundAmount,
                refund.getStatus().name(),
                "결제 취소 및 환불이 완료되었습니다."
        );
    }


    //부분환불로직
    private RefundResponse partialRefund(
            Payment payment,
            Order order,
            List<OrderItem> orderItems,
            RefundRequest request
    ) {

        Map<Long, OrderItem> orderItemMap =
                orderItems.stream()
                        .collect(Collectors.toMap(
                                OrderItem::getId,
                                Function.identity()
                        ));

        long totalRefundAmount = 0L;

        for (RefundItemRequest itemRequest : request.items()) {

            OrderItem orderItem = orderItemMap.get(itemRequest.orderItemId());

            if (orderItem == null) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_ITEM);
            }

            int alreadyRefunded = refundItemRepository.sumRefundedQuantity(orderItem.getId());

            int remainingQuantity = orderItem.getQuantity() - alreadyRefunded;

            if (itemRequest.quantity() > remainingQuantity) {
                throw new BusinessException(ErrorCode.REFUND_QUANTITY_MISMATCH);
            }

            long itemRefundAmount = orderItem.getProductPrice() * itemRequest.quantity();

            totalRefundAmount += itemRefundAmount;
        }

        long alreadyRefundedAmount = refundRepository.sumRefundedAmount(payment.getId());

        if (alreadyRefundedAmount + totalRefundAmount > payment.getFinalPrice()) {
            throw new BusinessException(ErrorCode.REFUND_AMOUNT_MISMATCH);
        }

        Refund refund = new Refund(
                payment,
                ZERO_POINT,
                Math.toIntExact(totalRefundAmount),
                request.reason(),
                LocalDateTime.now()
        );

        refundRepository.save(refund);

        for (RefundItemRequest itemRequest : request.items()) {

            OrderItem orderItem = orderItemMap.get(itemRequest.orderItemId());

            long itemRefundAmount = orderItem.getProductPrice() * itemRequest.quantity();

            RefundItem refundItem = new RefundItem(
                    refund,
                    orderItem,
                    itemRequest.quantity(),
                    ZERO_POINT,
                    itemRefundAmount
            );

            refundItemRepository.save(refundItem);

            Product product = orderItem.getProduct();
            product.restoreStock(itemRequest.quantity());
        }

        long totalRefunded = alreadyRefundedAmount + totalRefundAmount;

        //부분환불이 전부 완료됐을경우 환불로 처리
        boolean fullyRefunded = totalRefunded >= payment.getFinalPrice();
        if (fullyRefunded) {
            payment.markAsCancelled();
            order.cancel();
        } else {
            payment.markAsPartCancelled();
        }

        return new RefundResponse(
                payment.getId(),
                order.getId(),
                order.getStatus().name(),
                payment.getStatus().name(),
                totalRefundAmount,
                refund.getStatus().name(),
                fullyRefunded ? "결제 취소 및 환불이 완료되었습니다." : "부분 결제 취소 및 환불이 완료되었습니다."
        );
    }
}