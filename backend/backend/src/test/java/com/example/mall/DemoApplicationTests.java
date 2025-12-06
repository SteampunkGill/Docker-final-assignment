package com.example.mall; // <-- 已根据你的错误日志更正为 com.example.mall

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DemoApplicationTests {

    /**
     * 这个测试用于验证 Spring Boot 的应用上下文能否被成功加载。
     * 即使方法体为空，它本身也是一个有效的测试用例。
     */
    @Test
    void contextLoads() {
    }

    /**
     * 这是一个简单的断言测试，用于确保测试框架正常工作，
     * 并为 Jenkins 提供一个可以明确报告成功或失败的测试用例。
     */
    @Test
    void simpleAssertionTest() {
        // 准备数据
        String expectedMessage = "Hello JUnit 5";
        int sum = 2 + 2;

        // 执行断言
        assertEquals("Hello JUnit 5", expectedMessage, "字符串值不匹配");
        assertEquals(4, sum, "数学计算结果不正确");
    }
}