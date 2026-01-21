package com.example.market.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private Long userId; // Go中通常ID是int/uint，Java数据库ID通常用Long
    private Integer role;
}