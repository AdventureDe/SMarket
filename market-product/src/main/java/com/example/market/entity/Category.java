package com.example.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("category")
public class Category {
    @TableId(value = "category_id", type = IdType.AUTO)
    private Long categoryId;

    /**
     * 父分类ID
     * 0 代表是一级分类
     * 非 0 代表是该ID的子分类
     */
    private Long parentId;

    private String name;

    private Integer sortOrder;
}