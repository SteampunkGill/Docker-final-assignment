package com.example.mall.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.module.user.entity.Address;

import java.util.List;

public interface AddressService extends IService<Address> {
    List<Address> listByUserId(Long userId);
    boolean addAddress(Long userId, Address address);
}