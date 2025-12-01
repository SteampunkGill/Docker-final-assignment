package com.example.mall.module.order.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderCreateDTO {
    private List<Long> cartIds;
    private Long addressId;
}