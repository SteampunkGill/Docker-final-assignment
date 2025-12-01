package com.example.mall.module.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.mall.common.api.R;
import com.example.mall.module.product.entity.Product;
import com.example.mall.module.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public R<Page<Product>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId) {

        log.info("【数据流入】接收到商品列表分页查询请求: page={}, size={}, name={}, categoryId={}", page, size, name, categoryId);
        Page<Product> pageInfo = new Page<>(page, size);
        Page<Product> resultPage = productService.listProductsByPage(pageInfo, name, categoryId);
        return R.success("查询成功", resultPage);
    }

    @GetMapping("/{id}")
    public R<Product> getById(@PathVariable Long id) {
        log.info("【数据流入】接收到查询商品详情请求: id={}", id);
        Product product = productService.getById(id);
        log.info("【数据查询】根据ID'{}'查询到商品: {}", id, product != null);
        if (product == null) {
            return R.failed("商品不存在");
        }
        return R.success("查询成功", product);
    }

    // 后台管理接口示例

    @PostMapping("/admin") // 蓝图中为/admin/products，为简化暂不加前缀，实际项目中应加权限控制
    public R<Void> create(@RequestBody Product product) {
        log.info("【数据流入】(Admin)接收到新增商品请求: {}", product);
        boolean success = productService.save(product);
        return success ? R.success("新增成功", null) : R.failed("新增失败");
    }

    @PutMapping("/admin/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody Product product) {
        log.info("【数据流入】(Admin)接收到更新商品请求: id={}, data={}", id, product);
        product.setId(id);
        boolean success = productService.updateById(product);
        return success ? R.success("更新成功", null) : R.failed("更新失败");
    }

    @DeleteMapping("/admin/{id}")
    public R<Void> delete(@PathVariable Long id) {
        log.info("【数据流入】(Admin)接收到删除商品请求: id={}", id);
        boolean success = productService.removeById(id);
        return success ? R.success("删除成功", null) : R.failed("删除失败");
    }
}