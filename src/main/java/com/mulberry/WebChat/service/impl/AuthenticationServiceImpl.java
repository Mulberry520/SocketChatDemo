package com.mulberry.WebChat.service.impl;

import com.mulberry.WebChat.common.CommonConst;
import com.mulberry.WebChat.dto.UserChangePasswdReq;
import com.mulberry.WebChat.dto.UserLoginReq;
import com.mulberry.WebChat.dto.UserRegisterReq;
import com.mulberry.WebChat.exception.BusinessException;
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
    public void register(UserRegisterReq registerInfo) {
        String username = registerInfo.getUsername();
        String password = registerInfo.getPassword();
        String phone = registerInfo.getPhone();

        if (userMapper.selectIdByName(username) != null) {
            throw new BusinessException("Username already exists");
        }
        if (userMapper.selectIdByPhone(phone) != null) {
            throw new BusinessException("Phone number was bounded");
        }

        String encodedPassword = passwordEncoder.encode(password);
        if (userMapper.insertBasicInfo(username, encodedPassword, phone) != 1) {
            throw new BusinessException("Create new user failed");
        }
    }

    @Override
    public String login(UserLoginReq loginInfo, HttpServletResponse response) {
        String username = loginInfo.getUsername();
        String password = loginInfo.getPassword();
        Long userId = userMapper.selectIdByName(username);

        if (userId == null) {
            throw new BusinessException("Username not exists");
        }
        if (!passwordEncoder.matches(password, userMapper.selectPasswdById(userId))) {
            throw new BusinessException("Wrong password");
        }

        String refreshToken = jwtUtil.generateRefreshToken();
        addUserRoleToRedis(username, CommonConst.DEFAULT_ROLE);         // User role is scalable
        addRefreshTokenToRedis(refreshToken, username);
        addRefreshTokenToCookie(response, refreshToken);

        userMapper.updateStatusByName(CommonConst.STATUS_ONLINE, username);
        return jwtUtil.generateAccessToken(username);
    }

    @Override
    public String refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = extraTokenFromCookie(request);
        if (oldRefreshToken == null) {
            throw new IllegalArgumentException("No refresh token in cookie");
        }
        String username = template.opsForValue().get(CommonConst.REDIS_REFRESH_PREFIX + oldRefreshToken);
        if (username == null || username.isEmpty()) {
            throw new BusinessException(CommonConst.TOKEN_EXPIRE, "Expired refresh token");
        }

        template.delete(CommonConst.REDIS_REFRESH_PREFIX + oldRefreshToken);
        String newRefreshToken = jwtUtil.generateRefreshToken();
        addRefreshTokenToRedis(newRefreshToken, username);
        updateUserRole(username);
        String newAccessToken = jwtUtil.generateAccessToken(username);

        addRefreshTokenToCookie(response, newRefreshToken);
        return newAccessToken;
    }

    @Override
    public void logout(HttpServletRequest request) {
        String refreshToken = extraTokenFromCookie(request);
        if (refreshToken == null) {
            throw new IllegalArgumentException("No refresh token in cookie");
        }
        String username = template.opsForValue().get(CommonConst.REDIS_REFRESH_PREFIX + refreshToken);
        if (username == null || username.isEmpty()) {
            throw new BusinessException("Refresh Token expired");
        }

        template.delete(CommonConst.REDIS_REFRESH_PREFIX + refreshToken);
        template.delete(CommonConst.USER_ROLE_PREFIX + username);
        userMapper.updateStatusByName(CommonConst.STATUS_OFFLINE, username);
    }

    @Override
    public void changePassword(UserChangePasswdReq changes, HttpServletRequest request) {
        String newPassword = changes.getNewPassword();
        if (!newPassword.equals(changes.getConfirmPassword())) {
            throw new IllegalArgumentException("Confirm new password again");
        }

        String username = changes.getUsername();
        Long userId = userMapper.selectIdByName(username);
        if (userId == null) {
            throw new BusinessException("User not exists");
        }

        String oldPassword = changes.getOldPassword();
        if (!passwordEncoder.matches(oldPassword, userMapper.selectPasswdById(userId))) {
            throw new BusinessException("Password wrong");
        }
        if (userMapper.updatePasswordById(userId, passwordEncoder.encode(newPassword)) != 1) {
            throw new BusinessException("Change password failed");
        }

        String refreshToken = extraTokenFromCookie(request);
        template.delete(CommonConst.REDIS_REFRESH_PREFIX + refreshToken);
        template.delete(CommonConst.USER_ROLE_PREFIX + username);
        userMapper.updateStatusByName(CommonConst.STATUS_OFFLINE, username);
    }

    private String extraTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        String refreshToken = null;
        for (Cookie cookie : cookies) {
            if (CommonConst.REFRESH_COOKIE.equals(cookie.getName())) {
                refreshToken = cookie.getValue();
                break;
            }
        }
        return refreshToken;
    }

    private void addUserRoleToRedis(String username, String role) {
        template.opsForValue().set((CommonConst.USER_ROLE_PREFIX + username), role, jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);
    }

    private void updateUserRole(String username) {
        template.expire((CommonConst.USER_ROLE_PREFIX + username), jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);
    }

    private void addRefreshTokenToRedis(String token, String username) {
        template.opsForValue().set((CommonConst.REDIS_REFRESH_PREFIX + token), username, jwtUtil.getRefreshExpire(), TimeUnit.MILLISECONDS);
    }

    private void addRefreshTokenToCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(CommonConst.REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(false)              // Product env should be true
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofMillis(jwtUtil.getRefreshExpire()))
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}