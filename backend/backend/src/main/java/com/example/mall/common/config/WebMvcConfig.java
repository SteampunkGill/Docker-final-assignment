package com.example.mall.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/users/login",
                        "/users/register", // 如果有注册接口
                        "/products/**",    // 商品列表和详情对所有用户可见
                        "/categories/**" // 商品分类对所有用户可见
                );
    }
}