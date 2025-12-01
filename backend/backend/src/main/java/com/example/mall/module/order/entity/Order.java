package com.example.mall.module.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.mall.module.user.entity.Address; // [1] 引入Address类
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
// import java.util.Map; // 不再需要Map

@Data
@TableName(value = "orders", autoResultMap = true)
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer status;

    // [2] 将字段类型从 Map<String, Object> 修改为 Address
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Address receiverInfo;

    private LocalDateTime paymentTime;
    private LocalDateTime shippingTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<OrderItem> items;
}