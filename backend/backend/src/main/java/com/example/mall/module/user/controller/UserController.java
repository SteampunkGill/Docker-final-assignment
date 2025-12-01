package com.example.mall.module.user.controller;

import com.example.mall.common.api.R;
import com.example.mall.common.util.UserContext;
import com.example.mall.module.user.dto.UserLoginDTO;
import com.example.mall.module.user.entity.User;
import com.example.mall.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public R<Map<String, String>> login(@RequestBody UserLoginDTO loginDTO) {
        log.info("【数据流入】接收到登录请求: username={}", loginDTO.getUsername());
        String token = userService.login(loginDTO);
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        return R.success("登录成功", tokenMap);
    }

    @GetMapping("/profile")
    public R<User> getProfile() {
        Long currentUserId = UserContext.getCurrentUserId();
        log.info("【数据流入】接收到获取个人信息请求, 用户ID: {}", currentUserId);
        User user = userService.getProfile(currentUserId);
        return R.success("查询成功", user);
    }
}