package com.example.mall.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.module.order.entity.Order;
import com.example.mall.module.product.entity.Product;
import org.apache.ibatis.annotations.Param;

// 注解已移除，因为SQL实现在XML中
public interface OrderMapper extends BaseMapper<Order> {

    Product selectProductForUpdate(@Param("productId") Long productId);

    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);
}