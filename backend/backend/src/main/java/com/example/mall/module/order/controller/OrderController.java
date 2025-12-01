package com.example.mall.module.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.api.R;
import com.example.mall.common.util.UserContext;
import com.example.mall.module.order.dto.OrderCreateDTO;
import com.example.mall.module.order.entity.Order;
import com.example.mall.module.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public R<Order> create(@RequestBody OrderCreateDTO createDTO) {
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到创建订单请求: userId={}, DTO={}", currentUserId, createDTO);
        Order order = orderService.createOrder(currentUserId, createDTO);
        return R.success("订单创建成功", order);
    }

    @GetMapping
    public R<Page<Order>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Integer status) {
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到订单列表查询请求: userId={}, page={}, size={}, status={}", currentUserId, page, size, status);
        Page<Order> pageInfo = new Page<>(page, size);
        Page<Order> resultPage = orderService.listUserOrders(currentUserId, pageInfo, status);
        return R.success("查询成功", resultPage);
    }

    @GetMapping("/{orderNo}")
    public R<Order> getDetail(@PathVariable String orderNo) {
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到订单详情查询请求: userId={}, orderNo={}", currentUserId, orderNo);
        Order order = orderService.getOrderDetailByOrderNo(currentUserId, orderNo);
        return R.success("查询成功", order);
    }

    @PostMapping("/{orderNo}/pay")
    public R<Void> pay(@PathVariable String orderNo) {
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到(模拟)支付请求: userId={}, orderNo={}", currentUserId, orderNo);
        orderService.payOrder(currentUserId, orderNo);
        return R.success("支付成功", null);
    }
}