package com.example.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.market.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 对应 Go 代码中的 GetOrders 原生 SQL 查询
     * 将 JOIN 结果扁平化查出，稍后在 Service 层进行 GroupBy 组装
     */
    @Select("SELECT o.order_id, o.created_at, o.status, o.total_price, o.address_id, " +
            "op.product_id, op.num as quantity, " +
            "sp.product_name, sp.image_url, sp.price " +
            "FROM orders o " +
            "JOIN order_products op ON op.order_id = o.order_id " +
            "JOIN products sp ON sp.product_id = op.product_id " + // 注意表名 products
            "WHERE o.user_id = #{userId} " +
            "ORDER BY o.created_at DESC")
    List<Map<String, Object>> getOrdersWithProducts(Long userId);
}