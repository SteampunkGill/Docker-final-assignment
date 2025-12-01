package com.example.mall.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.exception.CustomException;
import com.example.mall.module.cart.entity.Cart;
import com.example.mall.module.cart.service.CartService;
import com.example.mall.module.order.dto.OrderCreateDTO;
import com.example.mall.module.order.entity.Order;
import com.example.mall.module.order.entity.OrderItem;
import com.example.mall.module.order.mapper.OrderMapper;
import com.example.mall.module.order.service.OrderItemService;
import com.example.mall.module.order.service.OrderService;
import com.example.mall.module.product.entity.Product;
import com.example.mall.module.product.service.ProductService;
import com.example.mall.module.user.entity.Address;
import com.example.mall.module.user.service.AddressService;
// import com.fasterxml.jackson.core.type.TypeReference; // 不再需要
// import com.fasterxml.jackson.databind.ObjectMapper; // 不再需要
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
// import java.util.Map; // 不再需要
import java.util.Random;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private CartService cartService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private OrderItemService orderItemService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductService productService;

    @Override
    @Transactional
    public Order createOrder(Long userId, OrderCreateDTO createDTO) {
        log.info("【业务处理-开始】用户'{}'创建订单, DTO: {}", userId, createDTO);

        // 1. 数据校验: 购物车项和地址
        List<Cart> cartItems = cartService.listByIds(createDTO.getCartIds());
        log.info("【数据查询】根据cartIds查询到 {} 条购物车记录", cartItems.size());
        Assert.isTrue(!CollectionUtils.isEmpty(cartItems), "请选择要结算的商品");
        cartItems.forEach(cart -> Assert.isTrue(cart.getUserId().equals(userId), "购物车数据异常"));

        Address address = addressService.getById(createDTO.getAddressId());
        log.info("【数据查询】根据addressId查询到地址: {}", address != null);
        Assert.notNull(address, "收货地址不存在");
        Assert.isTrue(address.getUserId().equals(userId), "地址选择异常");

        // 2. 库存校验与锁定
        BigDecimal totalAmount = new BigDecimal("0.00");
        List<OrderItem> orderItems = new ArrayList<>();

        for (Cart cart : cartItems) {
            // 扣减库存
            int affectedRows = orderMapper.decreaseStock(cart.getProductId(), cart.getQuantity());
            if (affectedRows == 0) {
                Product product = productService.getById(cart.getProductId());
                log.error("【库存校验失败】商品'{}'库存不足", product.getName());
                throw new CustomException("商品 '" + product.getName() + "' 库存不足");
            }
            log.info("【数据写入】商品'{}'库存扣减成功", cart.getProductId());

            // 填充订单明细
            Product product = productService.getById(cart.getProductId());
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setImageUrl(product.getImageUrl());
            orderItems.add(item);
            totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(cart.getQuantity())));
        }

        // 3. 创建订单主表
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(0); // 待付款
        // 修正后的代码：直接设置 Address 对象
        order.setReceiverInfo(address);
        this.save(order);
        log.info("【数据写入】订单主表记录创建成功, 订单号: {}", order.getOrderNo());

        // 4. 批量创建订单明细表
        orderItems.forEach(item -> item.setOrderId(order.getId()));
        orderItemService.saveBatch(orderItems);
        log.info("【数据写入】批量创建订单明细 {} 条", orderItems.size());

        // 5. 清空已下单的购物车项
        cartService.removeByIds(createDTO.getCartIds());
        log.info("【数据写入】清空购物车记录: {}", createDTO.getCartIds());

        log.info("【业务处理-结束】订单'{}'创建成功", order.getOrderNo());
        return order;
    }

    @Override
    public Page<Order> listUserOrders(Long userId, Page<Order> page, Integer status) {
        log.info("【业务处理】分页查询用户'{}'的订单: page={}, status={}", userId, page.getCurrent(), status);
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getUserId, userId);
        queryWrapper.eq(status != null, Order::getStatus, status);
        queryWrapper.orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = this.page(page, queryWrapper);
        log.info("【数据查询】分页查询订单结果: 共{}条", orderPage.getTotal());
        return orderPage;
    }

    @Override
    public Order getOrderDetailByOrderNo(Long userId, String orderNo) {
        log.info("【业务处理】查询用户'{}'的订单详情: orderNo={}", userId, orderNo);
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
        orderQuery.eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId);
        Order order = this.getOne(orderQuery);
        log.info("【数据查询】根据订单号'{}'查询到订单: {}", orderNo, order != null);

        if (order != null) {
            LambdaQueryWrapper<OrderItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(OrderItem::getOrderId, order.getId());
            List<OrderItem> items = orderItemService.list(itemQuery);
            log.info("【数据查询】查询到订单明细 {} 条", items.size());
            order.setItems(items);
        }
        return order;
    }

    @Override
    public void payOrder(Long userId, String orderNo) {
        log.info("【业务处理】(模拟)用户'{}'支付订单: orderNo={}", userId, orderNo);
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<>();
        query.eq(Order::getOrderNo, orderNo).eq(Order::getUserId, userId);
        Order order = this.getOne(query);
        Assert.notNull(order, "订单不存在");
        Assert.isTrue(order.getStatus() == 0, "订单状态异常，无法支付");

        order.setStatus(1); // 更新为已付款
        order.setPaymentTime(LocalDateTime.now());
        boolean result = this.updateById(order);
        log.info("【数据写入】订单'{}'状态更新为已付款, 结果: {}", orderNo, result);
    }

    private String generateOrderNo() {
        return DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now())
                + (new Random().nextInt(9000) + 1000);
    }
}