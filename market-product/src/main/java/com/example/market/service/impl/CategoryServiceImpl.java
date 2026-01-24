package com.example.market.service.impl;

import com.example.market.dto.CategoryCreateDTO;
import com.example.market.dto.CategoryUpdateDTO;
import com.example.market.entity.Category;
import com.example.market.mapper.CategoryMapper;
import com.example.market.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.example.market.vo.CategoryVO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 新增分类
     */
    @Override
    public void addCategory(CategoryCreateDTO input) {
        if (!StringUtils.hasText(input.getName())) {
            throw new RuntimeException("分类名称不能为空");
        }

        Category category = new Category();
        category.setName(input.getName());

        // 处理层级：如果前端没传 parentId，默认为 0 (一级分类)
        if (input.getParentId() == null) {
            category.setParentId(0L);
        } else {
            category.setParentId(input.getParentId());
        }

        // 处理排序：默认 0
        category.setSortOrder(input.getSortOrder() == null ? 0 : input.getSortOrder());

        categoryMapper.insert(category);
    }

    /**
     * 修改分类
     */
    @Override
    public void updateCategory(CategoryUpdateDTO input) {
        if (input.getCategoryId() == null) {
            throw new RuntimeException("分类ID不能为空");
        }

        Category category = categoryMapper.selectById(input.getCategoryId());
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

        // 逐个判断修改 (只改不为空的)
        if (StringUtils.hasText(input.getName())) {
            category.setName(input.getName());
        }

        if (input.getParentId() != null) {
            // 防止自己设置自己为父亲 (死循环)
            if (input.getParentId().equals(category.getCategoryId())) {
                throw new RuntimeException("父节点不能是自己");
            }
            category.setParentId(input.getParentId());
        }

        if (input.getSortOrder() != null) {
            category.setSortOrder(input.getSortOrder());
        }

        categoryMapper.updateById(category);
    }

    @Override
    public List<CategoryVO> getCategoryPath(Long categoryId) {
        List<CategoryVO> path = new ArrayList<>();

        // 1. 检查 ID 是否有效
        if (categoryId == null) return path;

        // 2. 循环向上查找父节点
        Long currentId = categoryId;
        while (currentId != null && currentId != 0) {
            Category category = categoryMapper.selectById(currentId);
            if (category == null) break; // 查不到了就停

            // 转成 VO 加入列表
            path.add(CategoryVO.fromEntity(category));

            // 向上爬一层
            currentId = category.getParentId();
        }

        // 3. 因为是向上查的，顺序是 [iPhone, 手机, 数码]，需要反转一下
        Collections.reverse(path);
        return path;
    }

    @Override
    public List<CategoryVO> getCategoryTree() {
        // 1. 一次性查出数据库所有分类 (避免递归查库，性能高)
        List<Category> allCategories = categoryMapper.selectList(null);

        // 2. 转换成 VO 列表
        List<CategoryVO> allVOs = allCategories.stream()
                .map(CategoryVO::fromEntity)
                .collect(Collectors.toList());

        // 3. 组装成树 (Java Stream流式处理)
        // 思路：找到所有根节点，然后给每个根节点找孩子
        return allVOs.stream()
                .filter(node -> node.getParentId() == 0) // A. 只要一级分类
                .peek(node -> node.setChildren(getChildren(node, allVOs))) // B. 递归找孩子
                .sorted((a, b) -> a.getSortOrder() - b.getSortOrder()) // C. 排序
                .collect(Collectors.toList());
    }

    /**
     * 递归辅助方法：在所有节点中，找到 root 的子节点
     */
    private List<CategoryVO> getChildren(CategoryVO root, List<CategoryVO> all) {
        return all.stream()
                .filter(node -> node.getParentId().equals(root.getCategoryId())) // 找我有血缘关系的孩子
                .peek(node -> node.setChildren(getChildren(node, all))) // 递归：孩子再找孩子
                .sorted((a, b) -> a.getSortOrder() - b.getSortOrder()) // 排序
                .collect(Collectors.toList());
    }
}