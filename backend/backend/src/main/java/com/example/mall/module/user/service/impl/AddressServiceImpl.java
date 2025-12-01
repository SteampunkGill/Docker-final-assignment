package com.example.mall.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.module.user.entity.Address;
import com.example.mall.module.user.mapper.AddressMapper;
import com.example.mall.module.user.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> listByUserId(Long userId) {
        log.info("【业务处理】查询用户ID'{}'的地址列表", userId);
        LambdaQueryWrapper<Address> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Address::getUserId, userId);
        List<Address> addresses = this.list(queryWrapper);
        log.info("【数据查询】为用户ID'{}'查询到 {} 条地址", userId, addresses.size());
        return addresses;
    }

    @Override
    public boolean addAddress(Long userId, Address address) {
        log.info("【业务处理】为用户ID'{}'新增地址", userId);
        address.setUserId(userId);
        boolean result = this.save(address);
        log.info("【数据写入】地址新增操作结果: {}", result);
        return result;
    }
}