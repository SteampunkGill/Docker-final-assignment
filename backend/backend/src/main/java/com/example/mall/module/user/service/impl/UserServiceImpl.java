package com.example.mall.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.exception.CustomException;
import com.example.mall.common.util.JwtUtil;
import com.example.mall.module.user.dto.UserLoginDTO;
import com.example.mall.module.user.entity.User;
import com.example.mall.module.user.mapper.UserMapper;
import com.example.mall.module.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String login(UserLoginDTO loginDTO) {
        log.info("【业务处理】用户登录: {}", loginDTO.getUsername());

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = this.getOne(queryWrapper);
        log.info("【数据查询】根据用户名'{}'查询到用户: {}", loginDTO.getUsername(), user != null);

        Assert.notNull(user, "用户名或密码错误");

        // 注意：生产环境中密码应该是加密存储和比对的, e.g., using BCryptPasswordEncoder
        if (!loginDTO.getPassword().equals(user.getPassword())) {
            throw new CustomException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("【业务处理】为用户'{}'生成Token成功", loginDTO.getUsername());
        return token;
    }

    @Override
    public User getProfile(Long userId) {
        log.info("【业务处理】获取用户ID为'{}'的个人信息", userId);
        User user = this.getById(userId);
        log.info("【数据查询】根据ID'{}'查询到用户: {}", userId, user != null);

        if (user != null) {
            user.setPassword(null); // 安全起见，不返回密码
        }
        return user;
    }
}