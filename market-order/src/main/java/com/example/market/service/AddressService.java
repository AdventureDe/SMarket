package com.example.market.service;

import com.example.market.dto.AddressDTO;
import com.example.market.entity.Address;
import java.util.List;

public interface AddressService {
    Address createAddress(AddressDTO input);
    List<Address> getAddressItem(Long userId);
    void removeAddressItem(Long userId, Long addressId);
}