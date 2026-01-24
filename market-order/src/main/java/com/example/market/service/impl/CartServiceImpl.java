package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.market.dto.CartAddDTO;
import com.example.market.dto.CartItemResponseDTO;
import com.example.market.entity.CartItem;
import com.example.market.entity.Product;
import com.example.market.mapper.CartMapper;
import com.example.market.mapper.ProductMapper;
import com.example.market.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableAsync // 开启异步支持
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper; // 需要这个Mapper来查商品详情

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 自注入用于调用 @Async 方法
    @Autowired
    @Lazy
    private CartServiceImpl self;

    @Override
    public List<CartItemResponseDTO> getCartItems(Long userId) {
        String key = "cart:" + userId;
        long start = System.currentTimeMillis();

        // 1. 尝试从 Redis 获取
        // Go: HGetAll
        Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(key);

        if (!cartMap.isEmpty()) {
            log.info("Redis 响应时间: {}ms", System.currentTimeMillis() - start);
            return buildCartFromRedis(cartMap);
        }

        // 2. Redis 无数据时从 MySQL 加载
        start = System.currentTimeMillis();

        // MyBatis-Plus 没有直接的 JOIN，我们用逻辑拼装（或者在Mapper写XML）
        // 这里模拟 Go 的 JOIN 逻辑：先查 CartItem，再查 Product
        List<CartItem> cartItems = cartMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId));

        List<CartItemResponseDTO> results = new ArrayList<>();
        if (!cartItems.isEmpty()) {
            // 提取商品ID列表
            List<Long> productIds = cartItems.stream().map(CartItem::getProductId).collect(Collectors.toList());
            // 批量查询商品
            List<Product> products = productMapper.selectBatchIds(productIds);
            // 转为 Map 方便匹配
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getProductId, p -> p));

            // 组装结果
            for (CartItem item : cartItems) {
                Product p = productMap.get(item.getProductId());
                if (p != null) {
                    CartItemResponseDTO dto = new CartItemResponseDTO();
                    dto.setCartId(item.getId());
                    dto.setProductId(p.getProductId());
                    dto.setProductName(p.getProductName());
                    dto.setProductDescription(p.getProductDescription());
                    dto.setPrice(p.getPrice());
                    dto.setImageUrl(p.getImageUrl());
                    dto.setQuantity(item.getQuantity());
                    results.add(dto);
                }
            }
        }

        log.info("MySQL 响应时间: {}ms", System.currentTimeMillis() - start);

        // 3. 写入 Redis 缓存（异步）
        // Go: go s.cacheCartItems(...)
        self.cacheCartItems(userId, results);

        return results;
    }

    /**
     * 异步方法：将DB结果缓存到Redis
     * Go: cacheCartItems
     */
    @Async
    public void cacheCartItems(Long userId, List<CartItemResponseDTO> items) {
        if (items.isEmpty()) return;

        String key = "cart:" + userId;
        // Go: Pipeline
        // Spring RedisTemplate 默认支持 Pipeline，但简单的 putAll 也可以
        // 为了精确模拟 Go 的 HSet 循环：
        redisTemplate.executePipelined((org.springframework.data.redis.connection.RedisCallback<Object>) connection -> {
            for (CartItemResponseDTO item : items) {
                String field = String.valueOf(item.getProductId());
                String value = String.valueOf(item.getQuantity());
                // HSET key field value
                connection.hSet(key.getBytes(), field.getBytes(), value.getBytes());
            }
            // Expire
            connection.expire(key.getBytes(), 24 * 60 * 60); // 24 hours
            return null;
        });
    }

    /**
     * 从 Redis 数据构建响应
     * Go: buildCartFromRedis
     */
    private List<CartItemResponseDTO> buildCartFromRedis(Map<Object, Object> cartMap) {
        List<Long> productIds = new ArrayList<>();
        for (Object k : cartMap.keySet()) {
            productIds.add(Long.valueOf(k.toString()));
        }

        if (productIds.isEmpty()) return new ArrayList<>();

        // 从数据库获取商品详情
        List<Product> products = productMapper.selectBatchIds(productIds);

        List<CartItemResponseDTO> results = new ArrayList<>();
        for (Product p : products) {
            String qtyStr = (String) cartMap.get(String.valueOf(p.getProductId()));
            Integer qty = qtyStr != null ? Integer.valueOf(qtyStr) : 0;

            CartItemResponseDTO dto = new CartItemResponseDTO();
            dto.setProductId(p.getProductId());
            dto.setProductName(p.getProductName());
            dto.setProductDescription(p.getProductDescription());
            dto.setPrice(p.getPrice());
            dto.setImageUrl(p.getImageUrl());
            dto.setQuantity(qty);
            // 注意：Redis缓存模式下，cart_id 可能拿不到，除非存在 Redis value 里，这里暂空
            results.add(dto);
        }
        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(CartAddDTO input) {
        // 1. 参数验证
        if (input.getUserId() == null || input.getProductId() == null) {
            throw new RuntimeException("invalid user/product ID");
        }
        Integer quantity = (input.getQuantity() == null || input.getQuantity() <= 0) ? 1 : input.getQuantity();

        // 2. 检查商品是否存在
        Product product = productMapper.selectById(input.getProductId());
        if (product == null) {
            throw new RuntimeException("product not exist");
        }

        // 3. 更新数据库 (模拟 Go 的 ON DUPLICATE KEY UPDATE)
        // Java JPA/MyBatis 通常做法是先查后改
        CartItem existingItem = cartMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, input.getUserId())
                .eq(CartItem::getProductId, input.getProductId()));

        if (existingItem != null) {
            // 更新数量
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setUpdateTime(LocalDateTime.now());
            cartMapper.updateById(existingItem);
        } else {
            // 插入新条目
            CartItem newItem = new CartItem();
            newItem.setUserId(input.getUserId());
            newItem.setProductId(input.getProductId());
            newItem.setQuantity(quantity);
            newItem.setCreateTime(LocalDateTime.now());
            newItem.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(newItem);
        }

        // 4. 更新 Redis (原子操作 HIncrBy)
        String key = "cart:" + input.getUserId();
        redisTemplate.opsForHash().increment(key, String.valueOf(input.getProductId()), quantity);

        // 5. 设置过期时间
        redisTemplate.expire(key, 7, TimeUnit.DAYS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCartItem(Long userId, Long productId) {
        if (userId == null || productId == null) return;

        // 先操作数据库
        cartMapper.delete(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));

        // 再操作 Redis
        String key = "cart:" + userId;
        redisTemplate.opsForHash().delete(key, String.valueOf(productId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) throw new RuntimeException("数量必须大于0");

        // 先更新数据库
        CartItem item = cartMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, productId));

        if (item == null) {
            throw new RuntimeException("购物车中未找到该商品");
        }

        item.setQuantity(quantity);
        item.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(item);

        // 再更新 Redis (HSet)
        String key = "cart:" + userId;
        redisTemplate.opsForHash().put(key, String.valueOf(productId), String.valueOf(quantity));
    }
}