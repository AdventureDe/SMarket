package com.example.market.service;

import com.example.market.dto.AddressDTO;
import com.example.market.entity.Address;
import java.util.List;

/**
 * 收货地址服务接口
 * 负责处理用户收货地址的增删改查业务
 */
public interface AddressService {

    /**
     * 新增收货地址
     *
     * @param userId 当前登录用户的ID (从Token或上下文获取，确保安全)
     * @param input  前端传入的地址表单数据
     * @return 创建成功后的地址实体（包含生成的ID）
     */
    Address addAddress(Long userId, AddressDTO input);

    /**
     * 获取用户的所有收货地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<Address> listAddresses(Long userId);

    /**
     * 删除收货地址
     * 注意：需要校验该地址是否属于该用户，防止越权删除
     *
     * @param userId    用户ID
     * @param addressId 待删除的地址ID
     */
    void removeAddress(Long userId, Long addressId);

    /**
     * 修改收货地址
     * @param userId 当前用户ID (安全校验)
     * @param input 地址表单数据 (必须包含 addressId)
     * @return 修改后的地址
     */
    Address updateAddress(Long userId, AddressDTO input);
}