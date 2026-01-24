package com.example.market.controller;

import com.example.market.common.Result;
import com.example.market.dto.CategoryCreateDTO;
import com.example.market.dto.CategoryUpdateDTO;
import com.example.market.service.CategoryService;
import com.example.market.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // 1. 新增分类
    @PostMapping("/add")
    public Result<String> addCategory(@RequestBody CategoryCreateDTO input) {
        // 鉴权逻辑(建议加上)：只有管理员能操作
        // String token = request.getHeader("Authorization"); ...

        categoryService.addCategory(input);
        return Result.success("分类添加成功");
    }

    // 2. 修改分类
    @PostMapping("/update")
    public Result<String> updateCategory(@RequestBody CategoryUpdateDTO input) {
        categoryService.updateCategory(input);
        return Result.success("分类修改成功");
    }

    // 1. 获取完整的分类树 (前端用于级联选择器 / 侧边栏)
    @GetMapping("/tree")
    public Result<List<CategoryVO>> getTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    // 2. 获取分类路径 (前端用于面包屑导航 / 回显)
    @GetMapping("/path/{id}")
    public Result<List<CategoryVO>> getPath(@PathVariable Long id) {
        return Result.success(categoryService.getCategoryPath(id));
    }
}