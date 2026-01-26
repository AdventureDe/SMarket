package com.example.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 市场服务启动类
 */
@SpringBootApplication  // 重点：确保注解拼写正确
@EnableDiscoveryClient
public class MarketProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketProductApplication.class, args);
    }
}