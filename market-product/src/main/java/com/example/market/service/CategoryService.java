package com.example.market.service;

import com.example.market.dto.CategoryCreateDTO;
import com.example.market.dto.CategoryUpdateDTO;
import com.example.market.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {
    void addCategory(CategoryCreateDTO input);
    void updateCategory(CategoryUpdateDTO input);
    /**
     * 功能1：根据当前分类ID，获取完整的层级路径（面包屑）
     * 例如输入 101(iPhone)，返回 [数码VO, 手机VO, iPhoneVO]
     */
    List<CategoryVO> getCategoryPath(Long categoryId);

    /**
     * 功能2：获取所有分类的树形结构
     * 返回一级分类列表，每个一级分类里包含它的子子孙孙
     */
    List<CategoryVO> getCategoryTree();
}