package com.example.market.controller; // 修正了这里：去掉了 .product

import com.example.market.common.Result;
// 假设 DTO 和 Service 也同样调整到了 com.example.market 下
import com.example.market.dto.ProductAddDTO;
import com.example.market.dto.ProductRemoveDTO;
import com.example.market.entity.Product;
import com.example.market.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// 保持和 Go 一样的路由风格，不加类级别的 RequestMapping
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取首页商品
     */
    @GetMapping("/shouye")
    public Result<List<Product>> getShouyeProducts() {
        List<Product> list = productService.getActiveProducts();
        return Result.success(list);
    }

    /**
     * 获取管理员商品
     */
    @GetMapping("/admin/products")
    public Result<List<Product>> getAdminProducts() {
        List<Product> list = productService.getAdminProducts();
        return Result.success(list);
    }

    /**
     * 搜索商品
     */
    @GetMapping("/searchs")
    public Result<List<Product>> searchProducts(@RequestParam(value = "search", required = false) String search) {
        List<Product> list = productService.searchProducts(search);
        return Result.success(list);
    }

    /**
     * 添加商品
     */
    @PostMapping("/addProduct")
    public Result<Product> addProduct(@RequestBody ProductAddDTO input) {
        try {
            Product product = productService.addProduct(input);
            return Result.success(product, "商品添加成功");
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取用户自己的商品
     */
    @GetMapping("/ownProducts")
    public Result<List<Product>> getOwnProducts(@RequestParam("user_id") Long userId) {
        if (userId == null) {
            return Result.error(400, "用户 ID 未提供");
        }
        try {
            List<Product> list = productService.getUserProducts(userId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/removeProduct/{productId}")
    public Result<String> removeProduct(
            @PathVariable("productId") Long productId,
            @RequestBody ProductRemoveDTO input) {
        try {
            productService.removeProduct(productId, input.getUserId());
            return Result.success(null, "商品已删除");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }
}