package com.example.market.dto;

import lombok.Data;

@Data
public class AdminResetPwdDTO {
    private Long userId; // 重置谁的？
    private String newPassword; // 重置后的密码
}
