package com.example.market.service;

import com.example.market.dto.OrderCreateDTO;
import com.example.market.dto.OrderResponseDTO;
import java.util.List;

public interface OrderService {
    OrderResponseDTO createOrder(OrderCreateDTO input);
    void cancelOrder(Long orderId);
    List<OrderResponseDTO> getOrders(Long userId);
}