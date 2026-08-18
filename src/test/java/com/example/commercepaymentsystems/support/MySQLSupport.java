package com.example.commercepaymentsystems.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class MySQLSupport {
    @Bean
    @ServiceConnection
    public MySQLContainer mySqlContainer() {
        return new MySQLContainer("mysql:8.4")
                .withDatabaseName("test")
                .withUsername("root")
                .withPassword("test");
    }
}
