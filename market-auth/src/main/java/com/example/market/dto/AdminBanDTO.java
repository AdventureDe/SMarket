package com.example.market.dto;

import lombok.Data;

@Data
public class AdminBanDTO {
    private Long userId; // 要搞谁？
    private Integer status; // 1=解封, 0=封禁
}