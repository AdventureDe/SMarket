package com.example.market.controller;

import com.example.market.common.Result; // 引用公共返回对象
import com.example.market.dto.CartAddDTO;
import com.example.market.dto.CartItemResponseDTO;
import com.example.market.dto.CartUpdateDTO;
import com.example.market.entity.CartItem;
import com.example.market.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart") // 统一前缀，对应 Go 路由组
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取购物车项
     * Go: r.GET("/cart", h.GetCartItems)
     */
    @GetMapping
    public Result<Map<String, Object>> getCartItems(@RequestParam(value = "user_id", required = false) Long userId) {
        // Go: userIDStr == "" -> 401
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        try {
            List<CartItemResponseDTO> items = cartService.getCartItems(userId);
            // Go: c.JSON(http.StatusOK, gin.H{"items": items})
            Map<String, Object> map = new HashMap<>();
            map.put("items", items);
            return Result.success(map);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 添加商品到购物车
     * Go: r.POST("/cart", h.AddToCart)
     */
    @PostMapping
    public Result<Void> addToCart(@RequestBody CartAddDTO input) {
        // Go: ShouldBindJSON
        // 注意：Go代码并未在Handler里获取user_id query，而是假设Input里有或者在Service处理
        // 假设 Input JSON 里包含了 userId, productId, quantity
        try {
            cartService.addToCart(input);
            // Go: c.Status(http.StatusNoContent) -> 204
            // 这里为了统一格式，返回 200 成功即可
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 从购物车中删除商品
     * Go: r.DELETE("/cart/:product_id", h.RemoveCartItem)
     */
    @DeleteMapping("/{productId}")
    public Result<String> removeCartItem(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "user_id", required = false) Long userId) {

        if (productId == null) {
            return Result.error(400, "商品ID无效");
        }
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }

        try {
            cartService.removeCartItem(userId, productId);
            return Result.success(null, "商品已从购物车中移除");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    /**
     * 更新购物车项数量
     * Go: r.PUT("/cart/:product_id/quantity", h.UpdateCartItemQuantity)
     */
    @PutMapping("/{productId}/quantity")
    public Result<String> updateCartItemQuantity(
            @PathVariable("productId") Long productId,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestBody CartUpdateDTO body) {

        if (productId == null) {
            return Result.error(400, "商品ID无效");
        }
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        // Go: body.Quantity check
        if (body.getQuantity() == null) {
            return Result.error(400, "请求数据无效");
        }

        try {
            cartService.updateCartItemQuantity(userId, productId, body.getQuantity());
            return Result.success(null, "数量更新成功");
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }
}