package com.example.market.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.market.dto.AddressDTO;
import com.example.market.entity.Address;
import com.example.market.mapper.AddressMapper;
import com.example.market.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor // 1. 自动生成构造函数注入，替代 @Autowired
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    // 因为继承了 ServiceImpl<AddressMapper, Address>，其实可以直接使用 baseMapper，
    // 但为了代码清晰，保留 addressMapper 也可以，或者直接用 this.save() 等方法。
    private final AddressMapper addressMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) // 2. 涉及多步数据库操作（修改旧默认+插入新地址），需开启事务
    public Address addAddress(Long userId, AddressDTO input) {
        // --- 基础校验 ---
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        // 建议使用 @Validated 在 Controller 层校验，这里保留也可以作为双重保障
        if (!StringUtils.hasText(input.getRecipient()) || !StringUtils.hasText(input.getPhone())) {
            throw new RuntimeException("收件人和联系电话不能为空");
        }
        if (!StringUtils.hasText(input.getProvince()) || !StringUtils.hasText(input.getCity())) {
            throw new RuntimeException("省市信息不能为空");
        }

        // --- 核心业务逻辑：处理“默认地址”互斥 ---
        // 如果用户选择将当前新地址设为默认，则需要把该用户之前的所有默认地址设为非默认
        if (Boolean.TRUE.equals(input.getIsDefault())) {
            // update address set is_default = 0 where user_id = ?
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .set(Address::getIsDefault, false)); // 或者 0
        }

        // --- 对象转换 ---
        Address newAddress = new Address();
        // 3. 使用 BeanUtils 减少大量 set 代码 (前提是 DTO 和 Entity 字段名一致)
        BeanUtils.copyProperties(input, newAddress);

        // 4. 强制覆盖 UserId (安全关键)
        // 无论 DTO 里传什么，都以 Controller 传下来的 userId 为准
        newAddress.setUserId(userId);

        // 确保 ID 为空，让数据库自增
        newAddress.setAddressId(null);

        // --- 入库 ---
        this.save(newAddress); // 使用 MyBatis-Plus 提供的 Service 方法

        return newAddress;
    }

    @Override
    public List<Address> listAddresses(Long userId) {
        // 按照是否默认倒序(默认地址排第一)，然后按创建时间倒序
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreatedAt)); // 假设实体类有 createTime
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 关键：开启事务，保证删除和替补操作原子性
    public void removeAddress(Long userId, Long addressId) {
        if (userId == null || addressId == null) {
            throw new RuntimeException("参数错误");
        }

        // 1. 【先查询】确保地址存在，且属于当前用户
        // 这一步必须做，因为我们需要知道被删除的这个地址是不是“默认地址”
        Address addressToDelete = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getAddressId, addressId)
                .eq(Address::getUserId, userId));

        if (addressToDelete == null) {
            throw new RuntimeException("地址不存在或无权操作");
        }

        // 2. 【执行删除】
        addressMapper.deleteById(addressId);

        // 3. 【判断替补】如果刚才删除的是默认地址，则寻找“继承人”
        if (Boolean.TRUE.equals(addressToDelete.getIsDefault())) {
            // 策略：选取该用户剩下的地址中，最近创建的一个（或者按 ID 倒序）
            Address newDefaultAddress = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    // 排除掉刚才删掉的那个（虽然已经删了，但保险起见只查存在的）
                    .orderByDesc(Address::getCreatedAt) // 优先找最新的
                    .last("LIMIT 1")); // 只取一条

            // 如果还能找到地址（防止用户删光了所有地址的情况）
            if (newDefaultAddress != null) {
                // 4. 【更新替补】将其设为默认
                newDefaultAddress.setIsDefault(true);
                addressMapper.updateById(newDefaultAddress);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Address updateAddress(Long userId, AddressDTO input) {
        // 1. 校验 ID
        if (input.getAddressId() == null) {
            throw new RuntimeException("修改失败：缺少地址ID");
        }

        // 2. 权限与存在性校验
        Address oldAddress = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getAddressId, input.getAddressId())
                .eq(Address::getUserId, userId));

        if (oldAddress == null) {
            throw new RuntimeException("地址不存在或无权修改");
        }

        // 3. 处理“默认地址”互斥逻辑
        // 只有当 input 明确传了 true 时才执行互斥逻辑
        if (Boolean.TRUE.equals(input.getIsDefault())) {
            addressMapper.update(null, new LambdaUpdateWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .set(Address::getIsDefault, false));
        }

        // 4. 【核心修改】部分更新逻辑
        // 舍弃 BeanUtils.copyProperties，改为逐个判断
        // 只有当 input 中的字段不为 null (或不为空串) 时，才更新 oldAddress

        if (StringUtils.hasText(input.getRecipient())) {
            oldAddress.setRecipient(input.getRecipient());
        }
        if (StringUtils.hasText(input.getPhone())) {
            oldAddress.setPhone(input.getPhone());
        }
        if (StringUtils.hasText(input.getCountry())) {
            oldAddress.setCountry(input.getCountry());
        }
        if (StringUtils.hasText(input.getProvince())) {
            oldAddress.setProvince(input.getProvince());
        }
        if (StringUtils.hasText(input.getCity())) {
            oldAddress.setCity(input.getCity());
        }
        if (StringUtils.hasText(input.getDistrict())) {
            oldAddress.setDistrict(input.getDistrict());
        }
        if (StringUtils.hasText(input.getStreet())) {
            oldAddress.setStreet(input.getStreet());
        }
        if (StringUtils.hasText(input.getStamp())) {
            oldAddress.setStamp(input.getStamp());
        }

        // 对于 Boolean 类型，只要不为 null 就更新
        if (input.getIsDefault() != null) {
            oldAddress.setIsDefault(input.getIsDefault());
        }

        // 5. 执行更新
        // 此时 oldAddress 还是完整的对象，只是部分字段被修改了新值
        // MyBatis-Plus 会生成 Update 语句更新所有字段（因为对象里都有值），达到更新目的
        this.updateById(oldAddress);

        return oldAddress;
    }
}