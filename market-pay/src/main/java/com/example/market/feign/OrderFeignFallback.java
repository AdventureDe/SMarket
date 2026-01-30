package com.example.market.feign;

import com.example.market.common.Result;
import org.springframework.stereotype.Component;

/**
 * 订单服务Feign降级处理
 */
@Component
public class OrderFeignFallback implements OrderFeignClient {
    @Override
    public Result<Boolean> validateOrderStatus(Long orderId, Long userId) {
        return Result.error("调用订单服务失败，订单状态验证超时");
    }

    @Override
    public Result<Boolean> updateOrderPayStatus(Long orderId, String payNo) {
        return Result.error("调用订单服务失败，订单支付状态更新超时");
    }

    @Override
    public Result<Boolean> updateOrderRefundStatus(Long orderId, Integer refundStatus) {
        return Result.error("调用订单服务失败，订单退款状态更新超时");
    }
}