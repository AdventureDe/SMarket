package com.example.market.dto;

import lombok.Data;

@Data
public class CategoryUpdateDTO {
    // 更新必须传主键
    private Long categoryId;

    // 下面是可以修改的字段
    private String name;
    private Long parentId; // 允许修改父级（比如把“手机”从“数码”移动到“电器”）
    private Integer sortOrder;
    private Integer status;
}