package com.example.commercepaymentsystems.orders.service;

import com.example.commercepaymentsystems.cart.entity.CartItem;
import com.example.commercepaymentsystems.cart.service.CartService;
import com.example.commercepaymentsystems.common.exception.BusinessException;
import com.example.commercepaymentsystems.common.exception.ErrorCode;
import com.example.commercepaymentsystems.customers.entity.Customers;
import com.example.commercepaymentsystems.customers.repository.CustomersRepository;
import com.example.commercepaymentsystems.orders.dto.request.CreateOrderRequest;
import com.example.commercepaymentsystems.orders.dto.request.OrderPreviewRequest;
import com.example.commercepaymentsystems.orders.dto.response.CreateOrderResponse;
import com.example.commercepaymentsystems.orders.dto.response.OrderDetailResponse;
import com.example.commercepaymentsystems.orders.dto.response.OrderListResponse;
import com.example.commercepaymentsystems.orders.dto.response.OrderPreviewResponse;
import com.example.commercepaymentsystems.orders.entity.Order;
import com.example.commercepaymentsystems.orders.entity.OrderItem;
import com.example.commercepaymentsystems.orders.entity.OrderStatus;
import com.example.commercepaymentsystems.orders.repository.OrderItemRepository;
import com.example.commercepaymentsystems.orders.repository.OrderRepository;
import com.example.commercepaymentsystems.payments.entity.Payment;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import com.example.commercepaymentsystems.products.entity.Product;
import com.example.commercepaymentsystems.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomersRepository customersRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final PaymentService paymentService;

    public OrderPreviewResponse getOrderPreview(Long customerId, OrderPreviewRequest request) {
        List<Long> cartItemIds = request == null ? List.of() : request.cartItemIds();
        List<CartItem> cartItems = getValidateCartItems(customerId, cartItemIds);

        List<OrderPreviewResponse.OrderPreviewItemResponse> items =
                cartItems.stream()
                        .map(cartItem -> {
                            Product product = productService.findEntityById(cartItem.getProductId());
                            Long price = product.getPrice();
                            Integer quantity = cartItem.getQuantity();
                            Long subtotal =
                                    price * quantity;

                            return new OrderPreviewResponse.OrderPreviewItemResponse(
                                    product.getId(),
                                    product.getName(),
                                    price,
                                    quantity,
                                    subtotal
                            );
                        })
                        .toList();

        Long totalAmount = items.stream()
                .mapToLong(OrderPreviewResponse.OrderPreviewItemResponse::subtotal)
                .sum();

        return new OrderPreviewResponse(items, totalAmount);
    }

    @Transactional
    public CreateOrderResponse createOrder(
            Long customerId,
            CreateOrderRequest request
    ) {
        Customers customer = customersRepository.findById(customerId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.CUSTOMER_NOT_FOUND)
                );

        List<Long> cartItemIds = (request == null) ? List.of() : request.cartItemIds();
        List<CartItem> cartItems = getValidateCartItems(customerId, cartItemIds);

        long totalPrice = 0L;
        for (CartItem cartItem : cartItems) {
            Product product = productService.findEntityById(cartItem.getProductId());
            Integer quantity = cartItem.getQuantity();
            product.decreaseStock(quantity);
            long subtotal = product.getPrice() * quantity;
            totalPrice += subtotal;
        }

        Long pointUsed = request.pointUsed();
        if (pointUsed > totalPrice) {
            throw new BusinessException(ErrorCode.POINT_EXCEEDS_ORDER_AMOUNT);
        }
        customer.usePoint(pointUsed);

        String orderNumber = generateOrderNumber();

        Order order = new Order(
                customer,
                orderNumber,
                totalPrice,
                pointUsed
        );
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream()
                .map(cartItem ->
                        new OrderItem(
                                savedOrder,
                                productService.findEntityById(cartItem.getProductId()),
                                cartItem.getQuantity()
                        )
                )
                .toList();
        orderItemRepository.saveAll(orderItems);
        paymentService.createPayment(savedOrder, totalPrice, pointUsed);

        return new CreateOrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getTotalPrice(),
                savedOrder.getPointUsed(),
                savedOrder.getOrderStatus().name()
        );
    }

    public Page<OrderListResponse> getOrders(Long customerId, Pageable pageable) {

        return orderRepository
                .findByCustomer_IdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::toListResponse);
    }

    public OrderDetailResponse getOrder(
            Long customerId,
            Long orderId
    ) {
        Order order = orderRepository
                .findByIdAndCustomer_Id(
                        orderId,
                        customerId
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderDetailResponse.OrderItemResponse> orderItems =
                orderItemRepository.findByOrder_Id(orderId)
                        .stream()
                        .map(this::toItemResponse)
                        .toList();

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getPointUsed(),
                order.getOrderStatus().name(),
                order.getCreatedAt(),
                orderItems
        );
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrder_Id(orderId);
    }

    private OrderListResponse toListResponse(Order order) {
        return new OrderListResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getOrderStatus().name(),
                order.getCreatedAt()
        );
    }

    private OrderDetailResponse.OrderItemResponse toItemResponse(OrderItem orderItem) {
        return new OrderDetailResponse.OrderItemResponse(
                orderItem.getProductName(),
                orderItem.getProductPrice(),
                orderItem.getQuantity()
        );
    }

    private String generateOrderNumber() {
        return "ORD-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16);
    }

    private List<CartItem> getValidateCartItems(Long customerId, List<Long> cartItemIds) {
        List<CartItem> cartItems = cartItemIds.isEmpty()
                ? cartService.findCartEntities(customerId)
                : cartService.findCartEntitiesByIds(cartItemIds, customerId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        if (!cartItemIds.isEmpty() && cartItems.size() != cartItemIds.size()) {
            throw new BusinessException(ErrorCode.CART_ITEM_FORBIDDEN);
        }

        return cartItems;
    }

    @Transactional
    public void confirmOrder(Order order) {
        order.confirm();
    }

    @Transactional
    public void cancelOrder(Long customerId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(
                        () -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new BusinessException(ErrorCode.ORDER_ACCESS_DENIED);
        }
        if (order.getOrderStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATUS);
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrder_Id(orderId);
        for (OrderItem orderItem : orderItems) {
            orderItem.getProduct().restoreStock(orderItem.getQuantity());
        }

        order.cancel();
        Payment payment = paymentService.findByOrderIdWithOrder(orderId);
        payment.markAsCancelled();
    }

    @Transactional
    public void cancelOrder(Order order) {
        order.cancel();
    }
}