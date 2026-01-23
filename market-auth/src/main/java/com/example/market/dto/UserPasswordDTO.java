package com.example.market.dto;

import lombok.Data;

@Data
public class UserPasswordDTO {
    private String oldPassword; // 为了安全，必须校验旧密码
    private String newPassword;
}
