package com.example.commercepaymentsystems.domain.payment.controller;

//import com.example.commercepaymentsystems.common.config.JpaAuditingConfig;

import com.example.commercepaymentsystems.orders.entity.OrderStatus;
import com.example.commercepaymentsystems.payments.controller.PaymentController;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmRequest;
import com.example.commercepaymentsystems.payments.dto.PaymentConfirmResponse;
import com.example.commercepaymentsystems.payments.dto.PaymentResponse;
import com.example.commercepaymentsystems.payments.entity.PaymentStatus;
import com.example.commercepaymentsystems.payments.facade.PaymentFacade;
import com.example.commercepaymentsystems.payments.service.PaymentCommandService;
import com.example.commercepaymentsystems.payments.service.PaymentService;
import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
public class PaymentControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PaymentService service;
    @MockitoBean
    private PaymentCommandService commandService;
    @MockitoBean
    private PaymentFacade facade;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        Long customerId = 1L;

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                customerId,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser
    @DisplayName("결제 단건 조회 api 테스트 - GET /api/payments/{id}")
    void payment_search_api_test()  throws Exception {
        //given
        given(service.getPayment(any(), anyLong()))
                .willReturn(new PaymentResponse(
                        10000L,
                        PaymentStatus.IN_PROGRESS.name(),
                        LocalDateTime.now()
                ));

        //when&then
        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice").value(10000L))
                .andExpect(jsonPath("$.status").value(PaymentStatus.IN_PROGRESS.name()));
    }

    @Test
    @DisplayName("결제 승인 api 테스트 - POST /api/payments/comfirm")
    void payment_confirm_api_test() throws Exception {
        //given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                1L,
                "SUCCESS",
                10000L
        );
        PaymentConfirmResponse response = new PaymentConfirmResponse(
                1L,
                1L,
                10000L,
                PaymentStatus.PAID.name(),
                OrderStatus.CONFIRMED.name()
        );

        given(facade.paymentConfirm(anyLong(), any(PaymentConfirmRequest.class))).willReturn(response);

        //when&then
        mockMvc.perform(post("/api/payments/confirm")
                        .with(csrf())
                        .contentType(String.valueOf(ContentType.APPLICATION_JSON))
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(10000L))
                .andExpect(jsonPath("$.paymentStatus").value(PaymentStatus.PAID.name()))
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.CONFIRMED.name()));
    }

    @Test
    @DisplayName("결제 승인 api 테스트 - POST /api/payments/comfirm 실패 ")
    void payment_confirm_api_test_failure_validation_failure() throws Exception {
        //given
        PaymentConfirmRequest request = new PaymentConfirmRequest(
                1L,
                "SUCCESS",
                null
        );
        PaymentConfirmResponse response = new PaymentConfirmResponse(
                1L,
                1L,
                10000L,
                PaymentStatus.PAID.name(),
                OrderStatus.CONFIRMED.name()
        );

        given(facade.paymentConfirm(anyLong(), any(PaymentConfirmRequest.class))).willReturn(response);

        //when&then
        mockMvc.perform(post("/api/payments/confirm")
                        .with(csrf())
                        .contentType(String.valueOf(ContentType.APPLICATION_JSON))
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
