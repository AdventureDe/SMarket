package com.example.market.service;

import com.example.market.dto.CartAddDTO;
import com.example.market.dto.CartItemResponseDTO;
import com.example.market.dto.CartUpdateDTO; // 假设你有这个
import java.util.List;

public interface CartService {
    List<CartItemResponseDTO> getCartItems(Long userId);
    void addToCart(CartAddDTO input);
    void removeCartItem(Long userId, Long productId);
    void updateCartItemQuantity(Long userId, Long productId, Integer quantity);
    void removeCartItemsBatch(Long userId, List<Long> productIds);
}