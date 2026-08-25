package com.example.commercepaymentsystems.infra.portone.controller;

import com.example.commercepaymentsystems.common.config.SecurityConfig;
import com.example.commercepaymentsystems.common.jwt.filter.JwtAuthenticationFilter;
import com.example.commercepaymentsystems.infra.portone.config.PortOneConfig;
import com.example.commercepaymentsystems.infra.portone.config.PortOneProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PortOneConfigController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
        )
)
@Import({PortOneConfig.class})
@EnableConfigurationProperties(PortOneProperties.class)
@ActiveProfiles("test")
class PortOneConfigControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PortOneProperties portOneProperties;


    @Test
    @WithMockUser
    void getPortOneInfo() throws Exception {
        System.out.println("getPortOneInfo: " + portOneProperties.getStoreId());

        //when&then
        mockMvc.perform(get("/api/portone-info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storeId").isString())
                .andExpect(jsonPath("$.data.channelKey").isString());
    }
}