package com.example.mall.module.order.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.module.order.dto.OrderCreateDTO;
import com.example.mall.module.order.entity.Order;

public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     */
    Order createOrder(Long userId, OrderCreateDTO createDTO);

    /**
     * 分页查询用户订单
     */
    Page<Order> listUserOrders(Long userId, Page<Order> page, Integer status);

    /**
     * 根据订单号获取订单详情
     */
    Order getOrderDetailByOrderNo(Long userId, String orderNo);

    /**
     * (模拟)支付订单
     */
    void payOrder(Long userId, String orderNo);
}