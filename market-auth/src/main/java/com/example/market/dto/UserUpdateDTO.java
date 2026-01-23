package com.example.market.dto;

import lombok.Data;

@Data
public class UserUpdateDTO {
    private String nickname; // 只传需要改的
    private String avatar;
    private String email;
}