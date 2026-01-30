package com.example.market.feign;

import com.example.market.common.Result;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务Feign客户端
 * 用于支付模块与订单模块的微服务通信
 */
@FeignClient(name = "market-order", fallback = OrderFeignFallback.class) // 服务名对应订单模块
public interface OrderFeignClient {

    /**
     * 验证订单状态（是否为待支付）
     * @param orderId 订单ID
     * @param userId 用户ID
     * @return 验证结果
     */
    @PostMapping("/order/validateStatus")
    Result<Boolean> validateOrderStatus(@RequestParam("orderId") Long orderId, @RequestParam("userId") Long userId);

    /**
     * 更新订单支付状态为已支付
     * @param orderId 订单ID
     * @param payNo 支付单号
     * @return 更新结果
     */
    @PostMapping("/order/updatePayStatus")
    Result<Boolean> updateOrderPayStatus(@RequestParam("orderId") Long orderId, @RequestParam("payNo") String payNo);

    /**
     * 更新订单状态为退款中/已退款
     * @param orderId 订单ID
     * @param refundStatus 退款状态（1-退款中，2-已退款）
     * @return 更新结果
     */
    @PostMapping("/order/updateRefundStatus")
    Result<Boolean> updateOrderRefundStatus(@RequestParam("orderId") Long orderId, @RequestParam("refundStatus") Integer refundStatus);
}