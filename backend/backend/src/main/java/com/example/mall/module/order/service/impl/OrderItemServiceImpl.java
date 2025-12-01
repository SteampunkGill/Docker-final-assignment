package com.example.mall.module.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.module.order.entity.OrderItem;
import com.example.mall.module.order.mapper.OrderItemMapper;
import com.example.mall.module.order.service.OrderItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 订单明细服务实现类
 */
@Slf4j
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {
    // ServiceImpl 已提供所有基础 CRUD 方法, 无需额外实现
}