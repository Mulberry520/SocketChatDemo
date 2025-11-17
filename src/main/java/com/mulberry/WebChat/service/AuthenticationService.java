package com.mulberry.WebChat.service;

import com.mulberry.WebChat.dto.UserLoginReq;
import com.mulberry.WebChat.dto.UserRegisterReq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    String register(UserRegisterReq registerInfo);

    String login(UserLoginReq loginInfo, HttpServletResponse response);

    String refresh(HttpServletRequest request, HttpServletResponse response);

    String logout(HttpServletRequest request);
}
