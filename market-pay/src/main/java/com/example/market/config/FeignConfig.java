package com.example.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.Logger;

/**
 * Feign客户端配置
 */
@Configuration
public class FeignConfig {

    /**
     * 配置Feign日志级别
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // 打印完整日志（请求、响应、headers等）
    }
}