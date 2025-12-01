package com.example.mall.module.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.module.product.entity.Product;
import com.example.mall.module.product.mapper.ProductMapper;
import com.example.mall.module.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Page<Product> listProductsByPage(Page<Product> page, String name, Long categoryId) {
        log.info("【业务处理】分页查询商品: page={}, name={}, categoryId={}", page.getCurrent(), name, categoryId);

        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();

        // 按名称模糊查询
        queryWrapper.like(StringUtils.hasText(name), Product::getName, name);
        // 按分类ID精确查询
        queryWrapper.eq(categoryId != null, Product::getCategoryId, categoryId);
        // 按创建时间倒序
        queryWrapper.orderByDesc(Product::getCreateTime);

        Page<Product> resultPage = this.page(page, queryWrapper);
        log.info("【数据查询】分页查询商品结果: 共{}条", resultPage.getTotal());
        return resultPage;
    }
}