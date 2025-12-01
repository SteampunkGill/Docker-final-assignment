package com.example.mall.module.cart.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.exception.CustomException;
import com.example.mall.module.cart.entity.Cart;
import com.example.mall.module.cart.mapper.CartMapper;
import com.example.mall.module.cart.service.CartService;
import com.example.mall.module.product.entity.Product;
import com.example.mall.module.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Slf4j
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    @Autowired
    private ProductService productService;

    @Autowired
    private CartMapper cartMapper;

    @Override
    public void addProduct(Long userId, Long productId, Integer quantity) {
        log.info("【业务处理】用户'{}'添加商品'{}'到购物车, 数量:{}", userId, productId, quantity);

        Product product = productService.getById(productId);
        log.info("【数据查询】根据商品ID'{}'查询到: {}", productId, product != null);
        Assert.notNull(product, "商品不存在");
        if (product.getStock() < quantity) {
            throw new CustomException("商品库存不足");
        }

        // 检查购物车是否已存在该商品
        LambdaQueryWrapper<Cart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, productId);
        Cart existingCartItem = this.getOne(queryWrapper);

        if (existingCartItem != null) {
            log.info("【业务处理】购物车已存在该商品, 更新数量");
            int newQuantity = existingCartItem.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new CustomException("商品库存不足");
            }
            existingCartItem.setQuantity(newQuantity);
            boolean result = this.updateById(existingCartItem);
            log.info("【数据写入】更新购物车数量操作结果: {}", result);
        } else {
            log.info("【业务处理】购物车不存在该商品, 新增记录");
            Cart newCartItem = new Cart();
            newCartItem.setUserId(userId);
            newCartItem.setProductId(productId);
            newCartItem.setQuantity(quantity);
            boolean result = this.save(newCartItem);
            log.info("【数据写入】新增购物车记录操作结果: {}", result);
        }
    }

    @Override
    public List<Cart> getCartList(Long userId) {
        log.info("【业务处理】获取用户'{}'的购物车列表", userId);
        // 此处直接调用Mapper中定义的多表查询方法
        List<Cart> carts = cartMapper.selectCartListByUserId(userId);
        log.info("【数据查询】为用户'{}'查询到 {} 条购物车记录", userId, carts.size());
        return carts;
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        log.info("【业务处理】用户'{}'更新购物车项'{}'数量为: {}", userId, cartId, quantity);
        Assert.isTrue(quantity > 0, "商品数量必须大于0");

        Cart cartItem = this.getById(cartId);
        log.info("【数据查询】根据购物车ID'{}'查询到: {}", cartId, cartItem != null);
        Assert.notNull(cartItem, "购物车项不存在");

        if (!cartItem.getUserId().equals(userId)) {
            throw new CustomException("无权操作他人购物车");
        }

        Product product = productService.getById(cartItem.getProductId());
        log.info("【数据查询】根据商品ID'{}'查询到: {}", cartItem.getProductId(), product != null);
        Assert.notNull(product, "关联商品不存在");

        if (product.getStock() < quantity) {
            throw new CustomException("商品库存不足");
        }

        cartItem.setQuantity(quantity);
        boolean result = this.updateById(cartItem);
        log.info("【数据写入】更新购物车数量操作结果: {}", result);
    }

    @Override
    public void deleteProduct(Long userId, Long cartId) {
        log.info("【业务处理】用户'{}'删除购物车项'{}'", userId, cartId);

        Cart cartItem = this.getById(cartId);
        log.info("【数据查询】根据购物车ID'{}'查询到: {}", cartId, cartItem != null);
        // 即使为空也无需报错，幂等性
        if (cartItem == null || !cartItem.getUserId().equals(userId)) {
            log.warn("购物车项不存在或不属于当前用户, 操作忽略");
            return;
        }

        boolean result = this.removeById(cartId);
        log.info("【数据写入】删除购物车记录操作结果: {}", result);
    }
}