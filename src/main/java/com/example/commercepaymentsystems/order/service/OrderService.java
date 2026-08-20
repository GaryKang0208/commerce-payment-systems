package com.example.commercepaymentsystems.order.service;

import com.example.commercepaymentsystems.order.dto.request.CreateOrderRequest;
import com.example.commercepaymentsystems.order.dto.response.CreateOrderResponse;
import com.example.commercepaymentsystems.order.dto.response.OrderDetailResponse;
import com.example.commercepaymentsystems.order.dto.response.OrderListResponse;
import com.example.commercepaymentsystems.order.dto.response.OrderPreviewResponse;
import com.example.commercepaymentsystems.order.entity.Order;
import com.example.commercepaymentsystems.order.entity.OrderItem;
import com.example.commercepaymentsystems.order.repository.OrderItemRepository;
import com.example.commercepaymentsystems.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
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

    private final CustomerRepository customerRepository;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final PaymentService paymentService;


    // 주문 미리보기
    // 로그인한 고객의 장바구니 전체를 조회해서 현재 상품 가격 기준으로 예상 주문 금액을 계산한다.
    public OrderPreviewResponse getOrderPreview(Long customerId) {

        // 1. 고객의 장바구니 전체 조회
        List<CartItem> cartItems =
                cartItemRepository.findByCustomer_Id(customerId);

        // 2. 장바구니가 비어 있으면 미리보기 불가
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException(
                    "장바구니가 비어 있습니다."
            );
        }

        // 3. CartItem → OrderPreviewItemResponse 변환
        // 미리보기에서는 OrderItem 스냅샷 가격이 아니라 Product의 현재 가격을 사용한다.
        List<OrderPreviewResponse.OrderPreviewItemResponse> items =
                cartItems.stream()
                        .map(cartItem -> {

                            Product product = cartItem.getProduct();

                            Long price = product.getPrice();
                            Integer quantity = cartItem.getQuantity();

                            // 상품별 예상 주문금액
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


        // 4. 전체 예상 주문 금액 계산
        Long totalAmount =
                items.stream()
                        .mapToLong(
                                OrderPreviewResponse.OrderPreviewItemResponse::subtotal)
                        .sum();

        // 5. 미리보기 응답 반환
        return new OrderPreviewResponse(items, totalAmount);
    }


    /**
     * 주문 생성
     *
     * 하나의 트랜잭션 안에서
     *
     * 1. 고객 조회
     * 2. 장바구니 조회
     * 3. 상품별 재고 검증
     * 4. 재고 차감
     * 5. 총 주문금액 계산
     * 6. 주문 생성
     * 7. 주문상품 스냅샷 생성
     * 8. 결제 사전 기록 생성
     *
     * 중간에 예외가 발생하면 전체 작업이 롤백된다.
     */
    @Transactional
    public CreateOrderResponse createOrder(
            Long customerId,
            CreateOrderRequest request
    ) {

        // 1. 주문한 고객 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "고객 정보를 찾을 수 없습니다."
                        )
                );

        List<Long> cartItemIds = (request == null) ? List.of() : request.cartItemIds();

        // 2. 주문할 장바구니 상품 조회
        // cartItemIds가 비어 있다면 해당 고객의 전체 장바구니를 주문한다.
        List<CartItem> cartItems;

        if (cartItemIds.isEmpty()) {

            cartItems =
                    cartService.findCartEntities(customerId);

        } else {

            cartItems =
                    cartService.findCartEntitiesByIds(
                            cartItemIds,
                            customerId
                    );
        }

        // 장바구니가 비어 있다면 주문할 수 없다.
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("주문할 장바구니 상품이 없습니다.");
        }


        // 선택 주문인 경우
        // 요청한 cartItemIds 개수와 실제 조회한 개수가 다르다면 존재하지 않는 장바구니 상품 또는 다른 고객의 상품이 포함됐다는 뜻이다.
        if (!cartItemIds.isEmpty()
                && cartItems.size() != cartItemIds.size()) {

            throw new IllegalArgumentException("유효하지 않은 장바구니 상품이 포함되어 있습니다.");
        }

        // 3. 모든 상품 재고 검증
        // 여기서는 아직 재고를 차감하지 않는다.
        // 주문 상품 중 단 하나라도 재고가 부족하면 전체 주문을 실패시키기 위해 먼저 모든 상품의 재고를 검사한다.
        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();

            if (product.getStock() < cartItem.getQuantity()) {

                throw new IllegalArgumentException(
                        product.getName()
                                + " 상품의 재고가 부족합니다."
                );
            }
        }

        // 4. 재고 차감 + 총 주문금액 계산
        // 재고 검증이 모두 끝났기 때문에 이제 실제 재고를 차감한다.
        long totalPrice = 0L;

        for (CartItem cartItem : cartItems) {

            Product product = cartItem.getProduct();
            Integer quantity = cartItem.getQuantity();

            product.decreaseStock(quantity);

            // 총 주문금액은 반드시 서버에서 계산한다.
            // 클라이언트가 전달한 금액은 신뢰하지 않는다.
            long subtotal =
                    product.getPrice() * quantity;

            totalPrice += subtotal;
        }

        // 5. 주문번호 생성
        String orderNumber = generateOrderNumber();

        // 6. Order 생성
        // Order 생성자 내부에서 OrderStatus.PENDING_PAYMENT로 초기화된다.
        Order order = new Order(
                customer,
                orderNumber,
                totalPrice
        );

        Order savedOrder =
                orderRepository.save(order);

        // 7. 주문상품 생성
        // OrderItem 생성자에서 productName과 productPrice를 복사하여 주문 당시 상품 정보를 스냅샷으로 보관한다.
        List<OrderItem> orderItems =
                cartItems.stream()
                        .map(cartItem ->
                                new OrderItem(
                                        savedOrder,
                                        cartItem.getProduct(),
                                        cartItem.getQuantity()
                                )
                        )
                        .toList();

        orderItemRepository.saveAll(orderItems);

        // 8. 결제 사전 기록 생성
        // 결제 금액은 반드시 주문에서 계산한 totalAmount를 사용한다.
        Payment payment = new Payment(savedOrder, totalPrice);

        paymentRepository.save(payment);

        // 주문 생성 시 장바구니는 삭제하지 않는다.
        // 결제 실패 후 다시 결제할 수 있어야 하기 때문에 장바구니 삭제는 결제 성공 시점에 처리한다.

        // 9. 주문 생성 결과 반환
        return new CreateOrderResponse(
                savedOrder.getId(),
                savedOrder.getOrderNumber(),
                savedOrder.getTotalPrice(),
                savedOrder.getOrderStatus().name()
        );
    }


    // 내 주문 목록 조회
    // 최신 주문부터 조회한다.
    public List<OrderListResponse> getOrders(Long customerId) {

        return orderRepository
                .findByCustomer_IdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::toListResponse)
                .toList();
    }


    // 내 주문 상세 조회
    // customerId까지 조건으로 사용해서 다른 고객의 주문을 조회하지 못하게 한다.
    public OrderDetailResponse getOrder(
            Long customerId,
            Long orderId
    ) {

        Order order = orderRepository
                .findByIdAndCustomer_Id(
                        orderId,
                        customerId
                )
                .orElseThrow(
                        () -> new IllegalArgumentException("주문 정보를 찾을 수 없습니다.")
                );

        // 해당 주문의 주문상품 조회
        List<OrderDetailResponse.OrderItemResponse> orderItems =
                orderItemRepository.findByOrder_Id(orderId)
                        .stream()
                        .map(this::toItemResponse)
                        .toList();


        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getOrderStatus().name(),
                order.getCreatedAt(),
                orderItems
        );
    }


    // Order → OrderListResponse
    // 주문 목록에서는 상품 전체 상세가 필요하지 않으므로 주문 기본 정보만 반환한다.
    private OrderListResponse toListResponse(
            Order order
    ) {

        return new OrderListResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getOrderStatus().name(),
                order.getCreatedAt()
        );
    }


    // OrderItem → OrderItemResponse
    // Product 현재 가격을 사용하지 않고 OrderItem에 저장한 주문 당시 스냅샷을 사용한다.
    private OrderDetailResponse.OrderItemResponse toItemResponse(
            OrderItem orderItem
    ) {

        return new OrderDetailResponse.OrderItemResponse(
                orderItem.getProductName(),
                orderItem.getProductPrice(),
                orderItem.getQuantity()
        );
    }


    // 주문번호 생성
    // ERD의 VARCHAR(20)에 맞춰 20자 주문번호를 생성한다.
    // 예) ORD-a12bc34de56f7890
    private String generateOrderNumber() {

        return "ORD-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16);
    }
}