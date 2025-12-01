package com.example.mall.module.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.module.product.entity.Product;

public interface ProductService extends IService<Product> {
    Page<Product> listProductsByPage(Page<Product> page, String name, Long categoryId);
}