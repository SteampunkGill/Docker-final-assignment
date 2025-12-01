package com.example.mall.common.config;

import com.example.mall.common.api.ResultCode;
import com.example.mall.common.exception.CustomException;
import com.example.mall.common.util.JwtUtil;
import com.example.mall.common.util.UserContext;
import io.jsonwebtoken.Claims;
// 修正: 导入 jakarta.servlet 包
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("【认证失败】请求头无有效Token, URI: {}", request.getRequestURI());
            throw new CustomException(ResultCode.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            if (jwtUtil.isTokenExpired(claims)) {
                log.warn("【认证失败】Token已过期, Token: {}", token);
                throw new CustomException(ResultCode.UNAUTHORIZED);
            }
            Long userId = claims.get("userId", Long.class);
            UserContext.setCurrentUserId(userId);
            log.info("【认证成功】用户ID: {}, URI: {}", userId, request.getRequestURI());
            return true;
        } catch (Exception e) {
            log.error("【认证异常】Token解析失败, Token: {}, 错误: {}", token, e.getMessage());
            throw new CustomException(ResultCode.UNAUTHORIZED);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}