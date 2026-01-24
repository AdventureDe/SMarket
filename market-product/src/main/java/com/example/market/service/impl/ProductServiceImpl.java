package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.market.dto.ProductAddDTO;
import com.example.market.dto.ProductUpdateDTO;
import com.example.market.entity.Category;
import com.example.market.entity.Product;
import com.example.market.mapper.CategoryMapper;
import com.example.market.mapper.ProductMapper;
import com.example.market.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Product> getActiveProducts() {
        // 对应 Go: GetActiveProducts (假设逻辑是 isActive=true 且 isViolation=false)
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getIsActive, true)
                .eq(Product::getIsViolation, false));
    }

    @Override
    public List<Product> getAdminProducts() {
        // 对应 Go: GetAdminProducts (通常是所有商品，或者特定排序)
        return productMapper.selectList(null);
    }

    /**
     * 包含复杂的内存排序逻辑
     */
    @Override
    public List<Product> searchProducts(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }

        String search = keyword.trim();
        String[] terms = search.split("\\s+"); // 按空格拆分搜索词

        // 1. 数据库查询 (WHERE name LIKE %term% OR desc LIKE %term%)
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> {
            for (String term : terms) {
                w.or().like(Product::getProductName, term)
                        .or().like(Product::getProductDescription, term);
            }
        });

        List<Product> products = productMapper.selectList(wrapper);

        // 2. 内存排序 (迁移自 Go 的 sort.Slice)
        // 规则：名称完全匹配 > 名称匹配词数多 > 描述匹配词数多
        products.sort((a, b) -> {
            // 忽略大小写
            boolean aContains = a.getProductName().toLowerCase().contains(search.toLowerCase());
            boolean bContains = b.getProductName().toLowerCase().contains(search.toLowerCase());

            // 1. 名称完全匹配优先
            if (aContains && !bContains) return -1; // -1 代表 a 排在 b 前面
            if (!aContains && bContains) return 1;

            // 2. 名称匹配词数多的优先
            int aNameCount = countMatchingTerms(a.getProductName(), terms);
            int bNameCount = countMatchingTerms(b.getProductName(), terms);
            if (aNameCount != bNameCount) {
                return bNameCount - aNameCount; // 降序
            }

            // 3. 描述匹配词数多的优先
            int aDescCount = countMatchingTerms(a.getProductDescription(), terms);
            int bDescCount = countMatchingTerms(b.getProductDescription(), terms);
            return bDescCount - aDescCount; // 降序
        });

        return products;
    }

    // 辅助方法：计算匹配词数量 (对应 Go 的 countMatchingTerms)
    private int countMatchingTerms(String text, String[] terms) {
        if (text == null) return 0;
        int count = 0;
        String lowerText = text.toLowerCase();
        for (String term : terms) {
            if (lowerText.contains(term.toLowerCase())) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Product addProduct(ProductAddDTO input) {
        // DTO 转 Entity
        Product product = new Product();
        product.setUserId(input.getUserId());
        product.setProductName(input.getProductName());
        product.setProductDescription(input.getProductDescription());
        product.setOrigin(input.getOrigin());
        product.setPrice(input.getPrice());
        product.setSalesPeriod(input.getSalesPeriod());
        product.setImageUrl(input.getImageUrl());
        if (input.getCategoryId() == null) {
            throw new RuntimeException("必须选择一个商品分类");
        }

        // 查一下这个分类是否存在
        Category category = categoryMapper.selectById(input.getCategoryId());
        if (category == null) throw new RuntimeException("分类不存在");

        product.setCategoryId(input.getCategoryId());
        // 设置默认值
        product.setIsActive(true);
        product.setIsViolation(false);
        product.setPublishDate(LocalDateTime.now());

        productMapper.insert(product);
        return product;
    }

    @Override
    public List<Product> getUserProducts(Long userId) {
        return productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getUserId, userId));
    }

    @Override
    public void updateProduct(ProductUpdateDTO input) {
        // 1. 先校验 ID 是否存在
        if (input.getProductId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }

        // 2. 查出数据库里原始的商品数据
        Product product = productMapper.selectById(input.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 3. 逐个字段判断：如果 DTO 里传了值(hasText)，就覆盖；如果没传或为空串，就保持原样
        // 商品名称
        if (StringUtils.hasText(input.getProductName())) {
            product.setProductName(input.getProductName());
        }

        if (input.getCategoryId() != null) {
            // (可选) 严谨一点，应该先去 categoryMapper 查一下这个 ID 存不存在
            // if (categoryMapper.selectById(input.getCategoryId()) == null) throw ...

            product.setCategoryId(input.getCategoryId());
        }

        // 描述
        if (StringUtils.hasText(input.getProductDescription())) {
            product.setProductDescription(input.getProductDescription());
        }

        // 发货地
        if (StringUtils.hasText(input.getOrigin())) {
            product.setOrigin(input.getOrigin());
        }

        // 价格 (BigDecimal 是对象，判断 null 即可)
        if (input.getPrice() != null) {
            product.setPrice(input.getPrice());
        }

        // 售卖时间段
        if (StringUtils.hasText(input.getSalesPeriod())) {
            product.setSalesPeriod(input.getSalesPeriod());
        }

        // 图片链接
        if (StringUtils.hasText(input.getImageUrl())) {
            product.setImageUrl(input.getImageUrl());
        }

        // 4. 执行更新
        // MyBatis-Plus 的 updateById 会把这个对象的新状态写回数据库
        productMapper.updateById(product);
    }

    @Override
    public void removeProduct(Long productId, Long userId) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品 ID 无效"); // 对应 Go: "商品 ID 无效"
        }

        // 权限校验
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此商品");
        }
        productMapper.deleteById(productId);
    }

    @Override
    public void listProduct(long productId, Long userId){
        Product product = productMapper.selectById(productId);
        if (product==null){
            throw new RuntimeException("商品 ID 无效");
        }
        // 权限校验
        // 需要结合微服务，查询user的role，如果是管理员也可以上下架
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此商品");
        }
        productMapper.update(
                null,
                new UpdateWrapper<Product>()
                        .eq("product_id", productId)
                        .set("is_active", 1)
        );
    }

    @Override
    public void delistProduct(long productId, Long userId) {
        Product product = productMapper.selectById(productId);
        if (product==null){
            throw new RuntimeException("商品 ID 无效");
        }
        // 权限校验
        // 需要结合微服务，查询user的role，如果是管理员也可以上下架
        if (!product.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此商品");
        }
        productMapper.update(
                null,
                new UpdateWrapper<Product>()
                        .eq("product_id", productId)
                        .set("is_active", 0)
        );
    }
}