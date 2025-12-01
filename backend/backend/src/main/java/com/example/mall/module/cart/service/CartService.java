package com.example.mall.module.cart.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.module.cart.entity.Cart;

import java.util.List;

public interface CartService extends IService<Cart> {

    /**
     * 添加商品到购物车
     */
    void addProduct(Long userId, Long productId, Integer quantity);

    /**
     * 获取用户的购物车列表
     */
    List<Cart> getCartList(Long userId);

    /**
     * 更新购物车商品数量
     */
    void updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 从购物车删除商品
     */
    void deleteProduct(Long userId, Long cartId);
}