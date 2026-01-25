package com.example.market.client;

import com.example.market.common.Result;
import com.example.market.entity.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

// "market-product" 是你在 application.yml 里给商品服务起的 spring.application.name
@FeignClient(name = "market-product")
public interface ProductClient {

    // 这里的路径必须和 ProductController 里的一模一样
    @PostMapping("/product/batch")
    Result<List<Product>> getProductsByIds(@RequestBody List<Long> productIds);
}