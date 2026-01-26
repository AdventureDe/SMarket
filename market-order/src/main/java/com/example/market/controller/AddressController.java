package com.example.market.controller;

import com.example.market.common.Result;
import com.example.market.dto.AddressDTO;
import com.example.market.entity.Address;
import com.example.market.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 新增地址
     */
    @PostMapping("/add")
    public Result<Address> addAddress(@RequestHeader("user_id") Long userId,
                                      @RequestBody AddressDTO input) {
        // 将 Header 里的 userId 传入 Service
        Address address = addressService.addAddress(userId, input);
        return Result.success(address);
    }

    /**
     * 获取列表
     */
    @GetMapping("/list")
    public Result<List<Address>> listAddresses(@RequestHeader("user_id") Long userId) {
        return Result.success(addressService.listAddresses(userId));
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/{addressId}")
    public Result<String> removeAddress(@RequestHeader("user_id") Long userId,
                                        @PathVariable Long addressId) {
        addressService.removeAddress(userId, addressId);
        return Result.success("删除成功");
    }

    /**
     * 修改地址
     * PUT /address/update
     */
    @PutMapping("/update")
    public Result<Address> updateAddress(@RequestHeader("user_id") Long userId,
                                         @RequestBody AddressDTO input) {
        // 这里的 input 中必须包含 addressId
        Address updatedAddress = addressService.updateAddress(userId, input);
        return Result.success(updatedAddress);
    }
}