package com.mulberry.WebChat.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

@Component
@Data
public class JwtUtil {
    @Value("${custom.jwt.secret-key}")
    private String secretKey;
    @Value("${custom.jwt.access-expire}")
    private Long accessExpire;
    @Value("${custom.jwt.refresh-expire}")
    private Long refreshExpire;

    private SecretKey getSigningKey() {
        byte[] keyBytes = this.secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        try {
            return parseToken(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String generateAccessToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + this.accessExpire))
                .signWith(getSigningKey())
                .compact();
    }

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    public String generateRefreshToken() {
        StringBuilder refreshToken = new StringBuilder(64);
        for (int i = 0; i < 64; i++) {
            refreshToken.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return refreshToken.toString();
    }
}
