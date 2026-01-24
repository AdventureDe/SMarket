package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.market.dto.OrderCreateDTO;
import com.example.market.dto.OrderProductResponseDTO;
import com.example.market.dto.OrderResponseDTO;
import com.example.market.entity.Address;
import com.example.market.entity.Order;
import com.example.market.entity.OrderProduct;
import com.example.market.mapper.AddressMapper;
import com.example.market.mapper.OrderMapper;
import com.example.market.mapper.OrderProductMapper;
import com.example.market.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderProductMapper orderProductMapper;
    @Autowired
    private AddressMapper addressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDTO createOrder(OrderCreateDTO input) {
        // 1. 验证输入
        if (input.getUserId() == null || input.getUserId() == 0) {
            throw new RuntimeException("用户未登录");
        }
        if (input.getTotalPrice() == null || input.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("无效的订单总价");
        }

        // 验证数组长度
        if (input.getProductIds() == null || input.getProductQuantities() == null ||
                input.getProductIds().size() != input.getProductQuantities().size()) {
            throw new RuntimeException("商品ID与数量不匹配");
        }

        // 2. 处理地址 (Go: 如果AddressID为0，则查找默认地址)
        Long finalAddressId = input.getAddressId();
        if (finalAddressId == null || finalAddressId == 0) {
            Address defaultAddr = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, input.getUserId())
                    .eq(Address::getIsDefault, true));
            if (defaultAddr != null) {
                finalAddressId = defaultAddr.getAddressId();
            } else {
                // Go代码只打印了日志没报错，这里也可以选择报错，或者允许为空
                System.out.println("没有找到默认地址");
            }
        }

        // 3. 创建订单
        Order newOrder = new Order();
        newOrder.setUserId(input.getUserId());
        newOrder.setTotalPrice(input.getTotalPrice());
        newOrder.setStatus("待付款");
        newOrder.setPaymentStatus("未付款");
        newOrder.setAddressId(finalAddressId);
        newOrder.setCreatedAt(LocalDateTime.now());
        newOrder.setUpdatedAt(LocalDateTime.now());

        orderMapper.insert(newOrder);

        // 4. 插入 order_products 表
        // Go: 循环插入
        List<OrderProductResponseDTO> productResponseList = new ArrayList<>();

        for (int i = 0; i < input.getProductIds().size(); i++) {
            Long pid = input.getProductIds().get(i);
            Integer num = input.getProductQuantities().get(i);

            OrderProduct op = new OrderProduct();
            op.setOrderId(newOrder.getOrderId());
            op.setProductId(pid);
            op.setNum(num);
            orderProductMapper.insert(op);

            // 构建返回对象的一部分（虽然这里拿不到商品名和图片，Go代码里返回时也没去数据库查这些，
            // 只是返回了空结构体或者前端不展示，这里为了简单先只填ID）
            OrderProductResponseDTO pResp = new OrderProductResponseDTO();
            pResp.setProductId(pid);
            pResp.setQuantity(num);
            productResponseList.add(pResp);
        }

        // Kafka 部分已删除 (Go: sendAsyncMessages)

        // 5. 构建响应
        OrderResponseDTO response = new OrderResponseDTO();
        response.setOrderId(newOrder.getOrderId());
        response.setCreateAt(newOrder.getCreatedAt());
        response.setTotalPrice(newOrder.getTotalPrice());
        response.setAddressId(newOrder.getAddressId());
        response.setStatus(newOrder.getStatus());
        response.setProducts(productResponseList);

        return response;
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        order.setStatus("已取消");
        // Go: DB.Save(&order)
        orderMapper.updateById(order);
    }

    @Override
    public List<OrderResponseDTO> getOrders(Long userId) {
        // 调用 Mapper 自定义的联表查询
        // 结果是 List<Map<String, Object>>，每一行是一个 "订单+一个商品" 的扁平记录
        List<Map<String, Object>> rawRows = orderMapper.getOrdersWithProducts(userId);

        // 使用 LinkedHashMap 保持订单插入顺序 (Go: created_at DESC)
        Map<Long, OrderResponseDTO> orderMap = new LinkedHashMap<>();

        for (Map<String, Object> row : rawRows) {
            Long orderId = (Long) row.get("order_id");

            // 如果 Map 中没有这个订单，就新建一个 DTO
            OrderResponseDTO dto = orderMap.computeIfAbsent(orderId, k -> {
                OrderResponseDTO newDto = new OrderResponseDTO();
                newDto.setOrderId(orderId);
                // 处理时间类型转换 (MySQL驱动可能返回 Timestamp 或 LocalDateTime)
                Object createdAt = row.get("created_at");
                if (createdAt instanceof Timestamp) {
                    newDto.setCreateAt(((Timestamp) createdAt).toLocalDateTime());
                } else if (createdAt instanceof LocalDateTime) {
                    newDto.setCreateAt((LocalDateTime) createdAt);
                }

                newDto.setStatus((String) row.get("status"));
                newDto.setTotalPrice((BigDecimal) row.get("total_price"));
                newDto.setAddressId((Long) row.get("address_id"));
                newDto.setProducts(new ArrayList<>());
                return newDto;
            });

            // 添加商品信息
            OrderProductResponseDTO productDTO = new OrderProductResponseDTO();
            productDTO.setProductId((Long) row.get("product_id"));
            productDTO.setQuantity((Integer) row.get("quantity"));
            productDTO.setProductName((String) row.get("product_name"));
            productDTO.setImageUrl((String) row.get("image_url"));
            productDTO.setPrice((BigDecimal) row.get("price"));

            dto.getProducts().add(productDTO);
        }

        return new ArrayList<>(orderMap.values());
    }
}