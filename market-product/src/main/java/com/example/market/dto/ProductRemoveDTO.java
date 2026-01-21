package com.example.market.dto;

import lombok.Data;

@Data
public class ProductRemoveDTO {
    private Long userId; // 用于删除时的权限校验
}