package com.example.mall.module.product.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.module.product.entity.Category;
import com.example.mall.module.product.mapper.CategoryMapper;
import com.example.mall.module.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    // 基础的CRUD操作由ServiceImpl提供，暂无需扩展
}