package com.example.market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 市场服务启动类
 */
@SpringBootApplication  // 重点：确保注解拼写正确，有@符号，没有多余字符
public class MarketProductApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketProductApplication.class, args);
    }
}