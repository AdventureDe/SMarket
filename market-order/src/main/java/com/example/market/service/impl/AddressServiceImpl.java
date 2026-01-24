package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.market.dto.AddressDTO;
import com.example.market.entity.Address;
import com.example.market.mapper.AddressMapper;
import com.example.market.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Override
    public Address createAddress(AddressDTO input) {
        // Go: 参数校验
        if (input.getUserId() == null || input.getUserId() == 0) {
            throw new RuntimeException("用户未登录");
        }
        if (!StringUtils.hasText(input.getRecipient()) || !StringUtils.hasText(input.getPhone())) {
            throw new RuntimeException("个人信息不能为空");
        }
        if (!StringUtils.hasText(input.getCountry()) || !StringUtils.hasText(input.getProvince())) {
            throw new RuntimeException("地址不能为空");
        }

        Address newAddress = new Address();
        newAddress.setUserId(input.getUserId());
        newAddress.setRecipient(input.getRecipient());
        newAddress.setPhone(input.getPhone());
        newAddress.setCountry(input.getCountry());
        newAddress.setProvince(input.getProvince());
        newAddress.setCity(input.getCity());
        newAddress.setDistrict(input.getDistrict());
        newAddress.setStreet(input.getStreet());
        newAddress.setIsDefault(input.getIsDefault());
        newAddress.setStamp(input.getStamp());

        addressMapper.insert(newAddress);
        return newAddress;
    }

    @Override
    public List<Address> getAddressItem(Long userId) {
        // Go: select specific fields... Where user_id = ?
        // MyBatis-Plus selectList 默认查所有字段，也可以用 queryWrapper.select(...) 指定
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId));
    }

    @Override
    public void removeAddressItem(Long userId, Long addressId) {
        if (userId == null || addressId == null) {
            throw new RuntimeException("无效的用户或地址ID");
        }

        // Go: Where(user_id, address_id).First(). Delete()
        // 这里直接 Delete 加条件即可
        int rows = addressMapper.delete(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getAddressId, addressId));

        if (rows == 0) {
            throw new RuntimeException("未找到该地址或已删除");
        }
    }
}