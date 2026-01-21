package com.example.market.dto;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String username;
    private String password;
    private Integer role;
}