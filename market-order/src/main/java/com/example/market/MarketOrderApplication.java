package com.example.market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
// 1. 扫描 Mapper 接口 (OrderMapper, CartMapper, AddressMapper, ProductMapper)
@MapperScan("com.example.market.mapper")
// 2. 开启异步任务支持 (对应 CartService 中的 @Async Redis缓存回写)
@EnableFeignClients
@EnableDiscoveryClient
@EnableAsync
public class MarketOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketOrderApplication.class, args);
    }
}