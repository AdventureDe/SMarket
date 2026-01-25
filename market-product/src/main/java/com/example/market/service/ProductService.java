package com.example.market.service;

import com.example.market.dto.ProductAddDTO;
import com.example.market.dto.ProductUpdateDTO;
import com.example.market.entity.Product;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

@Service
public interface ProductService extends IService<Product> {
    // 获取首页商品
    List<Product> getActiveProducts();
    List<Product> getAdminProducts();

    // 查询
    List<Product> searchProducts(String keyword);
    // Create
    Product addProduct(ProductAddDTO input);
    // Read
    List<Product> getUserProducts(Long userId);
    // Update
    void updateProduct(ProductUpdateDTO input);
    // Delete
    void removeProduct(Long productId, Long userId);

    // 上架 下架 is_active
    void listProduct(long productId, Long userId);
    void delistProduct(long productId, Long userId);

//    //微服务
//    List<Product> listByIds(List<Long> productIds);
}