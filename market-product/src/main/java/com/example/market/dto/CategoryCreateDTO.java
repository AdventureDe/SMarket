package com.example.market.dto;

import lombok.Data;

@Data
public class CategoryCreateDTO {
    // 父ID：如果不传，我们在Service层默认设为0（一级分类）
    private Long parentId;

    private String name;

    private Integer sortOrder;
}