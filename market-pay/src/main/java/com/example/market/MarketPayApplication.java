package com.example.market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 支付模块主类
 */
@SpringBootApplication
@MapperScan("com.example.market.mapper") // 扫描Mapper接口
@EnableFeignClients(basePackages = "com.example.market.feign") // 启用Feign客户端
public class MarketPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketPayApplication.class, args);
    }
}