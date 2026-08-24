package com.example.commercepaymentsystems.refund.service;

import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.orders.entity.OrderItem;
import com.example.commercepaymentsystems.orders.repository.OrderItemRepository;
import com.example.commercepaymentsystems.payments.repository.PaymentRepository;
import com.example.commercepaymentsystems.refund.dto.RefundItemRequest;
import com.example.commercepaymentsystems.refund.dto.RefundRequest;
import com.example.commercepaymentsystems.refund.dto.RefundResponse;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import com.example.commercepaymentsystems.refund.entity.Refund;
import com.example.commercepaymentsystems.refund.entity.RefundItem;
import com.example.commercepaymentsystems.refund.repository.RefundItemRepository;
import com.example.commercepaymentsystems.refund.repository.RefundRepository;
import com.example.commercepaymentsystems.cart.service.CartService;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.service.OrderService;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.service.ProductService;
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

    private final PaymentCommandService paymentCommandService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final CartService cartService;
    private final RefundRepository refundRepository;
    private final RefundItemRepository refundItemRepository;


    @Transactional
    public RefundResponse refund(
            Long paymentId,
            Long customerId,
            RefundRequest request
    ) {

        // 1. 결제 조회 + 본인 소유 검증
        Payment payment = paymentRepository
                .findByIdAndCustomerId(paymentId, customerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND)
                );

        // 2. 결제 상태 검증
        validatePaymentStatus(payment);

        Order order = payment.getOrder();

        // 3. 주문 상태 검증
        validateOrderOwner(order, customerId);

        // 4. 환불 대상 OrderItem 조회
        List<OrderItem> orderItems =
                orderItemRepository.findAllByOrderId(order.getId());

        // 5. 전액 / 부분 환불
        boolean fullRefund = request.items() == null || request.items().isEmpty();

        if (fullRefund) {
            return refundAll(
                    payment,
                    order,
                    orderItems,
                    request.reason()
            );
        }

        return partialRefund(
                payment,
                order,
                orderItems,
                request
        );
    }

    private void validatePaymentStatus(Payment payment) {

        if (payment.getStatus() != PaymentStatus.PAID
                && payment.getStatus() != PaymentStatus.PART_CANCELLED) {

            throw new BusinessException(
                    ErrorCode.INVALID_PAYMENT_STATUS
            );
        }
    }

    private void validateOrderOwner(
            Order order,
            Long customerId
    ) {

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_NOT_FOUND
            );
        }
    }

    private RefundResponse refundAll(
            Payment payment,
            Order order,
            List<OrderItem> orderItems,
            String reason
    ) {

        long alreadyRefunded =
                refundRepository.sumRefundedAmount(payment.getId());

        long refundAmount =
                payment.getFinalPrice() - alreadyRefunded;

        if (refundAmount <= 0) {
            throw new BusinessException(
                    ErrorCode.ALREADY_PROCESSED_REFUND
            );
        }

        // Refund 생성
        Refund refund = new Refund(
                payment,
                0,
                Math.toIntExact(refundAmount),
                reason,
                LocalDateTime.now()
        );

        refundRepository.save(refund);

        // 모든 상품의 남은 수량 계산 후 복구
        for (OrderItem orderItem : orderItems) {

            int refundedQuantity =
                    refundItemRepository.sumRefundedQuantity(
                            orderItem.getId()
                    );

            int remainingQuantity =
                    orderItem.getQuantity() - refundedQuantity;

            if (remainingQuantity <= 0) {
                continue;
            }

            long itemRefundAmount =
                    orderItem.getProductPrice()
                            * remainingQuantity;

            RefundItem refundItem = new RefundItem(
                    refund,
                    orderItem,
                    remainingQuantity,
                    0,
                    itemRefundAmount
            );

            refundItemRepository.save(refundItem);

            Product product = orderItem.getProduct();

            product.restoreStock(remainingQuantity);
        }

        payment.markAsCancelled();
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

            validateRefundRequest(itemRequest);

            OrderItem orderItem =
                    orderItemMap.get(itemRequest.orderItemId());

            if (orderItem == null) {
                throw new BusinessException(
                        ErrorCode.INVALID_REFUND_ITEM
                );
            }

            int alreadyRefunded =
                    refundItemRepository.sumRefundedQuantity(
                            orderItem.getId()
                    );

            int remainingQuantity =
                    orderItem.getQuantity() - alreadyRefunded;

            if (itemRequest.quantity() > remainingQuantity) {
                throw new BusinessException(
                        ErrorCode.REFUND_QUANTITY_MISMATCH
                );
            }

            long itemRefundAmount =
                    orderItem.getProductPrice()
                            * itemRequest.quantity();

            totalRefundAmount += itemRefundAmount;
        }

        // 전체 결제금액 초과 여부
        long alreadyRefundedAmount =
                refundRepository.sumRefundedAmount(
                        payment.getId()
                );

        if (alreadyRefundedAmount + totalRefundAmount
                > payment.getFinalPrice()) {

            throw new BusinessException(
                    ErrorCode.REFUND_AMOUNT_MISMATCH
            );
        }

        // Refund 생성
        Refund refund = new Refund(
                payment,
                0,
                Math.toIntExact(totalRefundAmount),
                request.reason(),
                LocalDateTime.now()
        );

        refundRepository.save(refund);

        // RefundItem 저장 + 재고 복구
        for (RefundItemRequest itemRequest : request.items()) {

            OrderItem orderItem =
                    orderItemMap.get(itemRequest.orderItemId());

            long itemRefundAmount =
                    orderItem.getProductPrice()
                            * itemRequest.quantity();

            RefundItem refundItem = new RefundItem(
                    refund,
                    orderItem,
                    itemRequest.quantity(),
                    0,
                    itemRefundAmount
            );

            refundItemRepository.save(refundItem);

            Product product = orderItem.getProduct();

            product.restoreStock(itemRequest.quantity());
        }

        // 현재까지의 환불금액
        long totalRefunded =
                alreadyRefundedAmount + totalRefundAmount;

        boolean fullyRefunded =
                totalRefunded >= payment.getFinalPrice();

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
                fullyRefunded
                        ? "결제 취소 및 환불이 완료되었습니다."
                        : "부분 결제 취소 및 환불이 완료되었습니다."
        );
    }

    private void validateRefundRequest(
            RefundItemRequest request
    ) {

        if (request.orderItemId() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REFUND_ITEM
            );
        }

        if (request.quantity() == null
                || request.quantity() <= 0) {

            throw new BusinessException(
                    ErrorCode.INVALID_REFUND_QUANTITY
            );
        }
    }
}
