package com.example.market.dto;

import lombok.Data; // 假设使用了Lombok，如果没有，请手动生成Getter/Setter

@Data
public class UserRegisterDTO {
    private String username;
    private String password;
    private String email;
    private String phone;
    private Integer role; // Go中的int对应Java的Integer
}