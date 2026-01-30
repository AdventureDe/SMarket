package com.example.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.market.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 支付记录Mapper
 */
@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
    /**
     * 根据订单ID查询支付记录
     * @param orderId 订单ID
     * @return 支付记录
     */
    Payment selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据支付单号查询支付记录
     * @param payNo 支付单号
     * @return 支付记录
     */
    Payment selectByPayNo(@Param("payNo") String payNo);
}