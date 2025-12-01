package com.example.mall.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // 从秘钥字符串生成 SecretKey
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // 从 Token 中提取所有 Claims
    private Claims getAllClaimsFromToken(String token) {
        // [修正] 使用新的 jjwt 0.12.x API
        // Jwts.parser() 返回一个 JwtParserBuilder 实例
        return Jwts.parser()
                .verifyWith(getSigningKey()) // 使用 verifyWith() 替代旧的 setSigningKey()
                .build()
                .parseSignedClaims(token) // 使用 parseSignedClaims() 替代旧的 parseClaimsJws()
                .getPayload(); // 使用 getPayload() 获取 Claims
    }

    // 从 Token 中提取指定的 Claim
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // 生成 Token
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // 验证 Token 是否过期
    public Boolean isTokenExpired(String token) {
        final Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    // [兼容旧代码的辅助方法]
    public Claims getClaimsFromToken(String token) {
        return getAllClaimsFromToken(token);
    }

    // [兼容旧代码的辅助方法]
    public boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}