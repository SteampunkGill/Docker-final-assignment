package com.example.mall.module.cart.controller;

import com.example.mall.common.api.R;
import com.example.mall.common.util.UserContext;
import com.example.mall.module.cart.entity.Cart;
import com.example.mall.module.cart.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public R<Void> add(@RequestBody Map<String, Object> payload) {
        Long productId = Long.valueOf(payload.get("productId").toString());
        Integer quantity = (Integer) payload.get("quantity");
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到添加购物车请求: userId={}, productId={}, quantity={}", currentUserId, productId, quantity);
        cartService.addProduct(currentUserId, productId, quantity);
        return R.success("添加成功", null);
    }

    @GetMapping
    public R<List<Cart>> list() {
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到查询购物车列表请求, 用户ID: {}", currentUserId);
        List<Cart> cartList = cartService.getCartList(currentUserId);
        return R.success("查询成功", cartList);
    }

    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        Integer quantity = payload.get("quantity");
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到更新购物车请求: userId={}, cartId={}, quantity={}", currentUserId, id, quantity);
        cartService.updateQuantity(currentUserId, id, quantity);
        return R.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到删除购物车请求: userId={}, cartId={}", currentUserId, id);
        cartService.deleteProduct(currentUserId, id);
        return R.success("删除成功", null);
    }
}