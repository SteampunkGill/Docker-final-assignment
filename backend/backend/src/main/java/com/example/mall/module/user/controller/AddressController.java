package com.example.mall.module.user.controller;

import com.example.mall.common.api.R;
import com.example.mall.common.util.UserContext;
import com.example.mall.module.user.entity.Address;
import com.example.mall.module.user.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public R<List<Address>> list() {
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到查询地址列表请求, 用户ID: {}", currentUserId);
        List<Address> addresses = addressService.listByUserId(currentUserId);
        return R.success("查询成功", addresses);
    }

    @PostMapping
    public R<Void> add(@RequestBody Address address) {
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到新增地址请求: {}, 用户ID: {}", address, currentUserId);
        addressService.addAddress(currentUserId, address);
        return R.success("添加成功", null);
    }
}