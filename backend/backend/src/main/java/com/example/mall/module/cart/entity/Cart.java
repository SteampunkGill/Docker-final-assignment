package com.example.mall.module.cart.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.mall.module.product.entity.Product;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("carts")
public class Cart {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 非数据库字段，用于连表查询显示商品信息
    @TableField(exist = false)
    private Product product;
}