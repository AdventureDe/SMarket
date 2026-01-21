package com.example.market.service;

import com.example.market.dto.ProductAddDTO;
import com.example.market.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> getActiveProducts();
    List<Product> getAdminProducts();

    // 之前提到的搜索逻辑 (虽然这个 Go service 文件里没写，但 handler 里有，所以这里保留)
    List<Product> searchProducts(String keyword);

    Product addProduct(ProductAddDTO input);
    List<Product> getUserProducts(Long userId);
    void removeProduct(Long productId, Long userId);
}