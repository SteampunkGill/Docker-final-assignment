package com.example.mall.module.cart.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.module.cart.entity.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper extends BaseMapper<Cart> {

    /**
     * 根据用户ID查询购物车列表（包含商品详情）
     * 该方法的SQL实现在 CartMapper.xml 中
     */
    List<Cart> selectCartListByUserId(@Param("userId") Long userId);
}