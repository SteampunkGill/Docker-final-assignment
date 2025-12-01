package com.example.mall.module.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.module.user.dto.UserLoginDTO;
import com.example.mall.module.user.entity.User;

public interface UserService extends IService<User> {
    String login(UserLoginDTO loginDTO);
    User getProfile(Long userId);
}