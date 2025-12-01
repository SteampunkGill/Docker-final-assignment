package com.example.mall.common.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局配置
 * 用于解决 LocalDateTime 的序列化问题
 */
@Configuration
public class JacksonConfig {

    /**
     * 添加对 Java 8 日期时间 API (JSR-310) 的支持
     * @return Jackson2ObjectMapperBuilderCustomizer
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> builder.modules(new JavaTimeModule());
    }
}