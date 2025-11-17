package com.mulberry.WebChat.service.impl;

import com.mulberry.WebChat.dto.UserLoginReq;
import com.mulberry.WebChat.dto.UserRegisterReq;
import com.mulberry.WebChat.mapper.ChatUserMapper;
import com.mulberry.WebChat.service.AuthenticationService;
import com.mulberry.WebChat.service.ChatUserService;
import com.mulberry.WebChat.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final ChatUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate template;
    private static final String REDIS_TOKEN_PREFIX = "refreshToken:";
    private static final String REFRESH_COOKIE = "refreshToken";

    public AuthenticationServiceImpl(
            ChatUserMapper chatUserMapper,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            StringRedisTemplate template
    ) {
        this.userMapper = chatUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.template = template;
    }

    @Override
    public String register(UserRegisterReq registerInfo) {
        String username = registerInfo.getUsername();
        String password = registerInfo.getPassword();
        String phone = registerInfo.getPhone();

        if (userMapper.selectIdByName(username) != null) {
            return "Username already exists";
        }
        if (userMapper.selectIdByPhone(phone) != null) {
            return "Phone number was bounded";
        }

        String encodedPassword = passwordEncoder.encode(password);
        int affected = userMapper.insertBasicInfo(username, encodedPassword, phone);
        if (affected != 1) {
            return "Create user failed";
        }
        return null;
    }

    @Override
    public String login(UserLoginReq loginInfo, HttpServletResponse response) {
        String username = loginInfo.getUsername();
        String password = loginInfo.getPassword();
        Long userId = userMapper.selectIdByName(username);

        if (userId == null) {
            throw new IllegalArgumentException("Username not exists");
        }
        if (!passwordEncoder.matches(password, userMapper.selectPasswdById(userId))) {
            throw new IllegalArgumentException("Wrong password");
        }

        String refreshToken = jwtUtil.generateRefreshToken();
        addRefreshTokenToRedis(refreshToken, username);
        addRefreshTokenToCookie(response, refreshToken);

        userMapper.updateStatusByName(ChatUserService.Status.ONLINE.getStatus(), username);
        return jwtUtil.generateAccessToken(username);
    }

    @Override
    public String refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = extraTokenFromCookie(request);
        if (oldRefreshToken == null) {
            throw new IllegalArgumentException("No refresh token");
        }
        String username = template.opsForValue().get(REDIS_TOKEN_PREFIX + oldRefreshToken);
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Expired refresh token");
        }

        template.delete(REDIS_TOKEN_PREFIX + oldRefreshToken);
        String newRefreshToken = jwtUtil.generateRefreshToken();
        addRefreshTokenToRedis(newRefreshToken, username);
        String newAccessToken = jwtUtil.generateAccessToken(username);

        addRefreshTokenToCookie(response, newRefreshToken);
        return newAccessToken;
    }

    @Override
    public String logout(HttpServletRequest request) {
        String refreshToken = extraTokenFromCookie(request);
        if (refreshToken == null) {
            return "No refresh token";
        }
        String username = template.opsForValue().get(REDIS_TOKEN_PREFIX + refreshToken);
        if (username == null || username.isEmpty()) {
            return "Refresh Token expired";
        }

        template.delete(REDIS_TOKEN_PREFIX + refreshToken);
        userMapper.updateStatusByName(ChatUserService.Status.OFFLINE.getStatus(), username);
        return null;
    }

    private String extraTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if (REFRESH_COOKIE.equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }
        return refreshToken;
    }

    private void addRefreshTokenToRedis(String token, String username) {
        template.opsForValue().set((REDIS_TOKEN_PREFIX + token), username, jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(false)              // Product env should be true
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofMillis(jwtUtil.getRefreshExpire()))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}
