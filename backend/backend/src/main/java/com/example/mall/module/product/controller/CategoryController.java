package com.example.mall.module.product.controller;

import com.example.mall.common.api.R;
import com.example.mall.module.product.entity.Category;
import com.example.mall.module.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public R<List<Category>> list() {
        log.info("【数据流入】接收到查询所有分类列表的请求");
        List<Category> list = categoryService.list();
        log.info("【数据查询】查询到 {} 条分类记录", list.size());
        return R.success("查询成功", list);
    }
}