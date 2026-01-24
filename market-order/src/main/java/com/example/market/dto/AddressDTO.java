package com.example.market.dto;

import lombok.Data;

@Data
public class AddressDTO {
    private Long userId;
    private String recipient;
    private String phone;
    private String country;
    private String province;
    private String city;
    private String district;
    private String street;
    private Boolean isDefault;
    private String stamp;
}