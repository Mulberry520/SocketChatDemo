package com.mulberry.WebChat.service;

import com.mulberry.WebChat.dto.UserChangePasswdReq;
import com.mulberry.WebChat.dto.UserLoginReq;
import com.mulberry.WebChat.dto.UserRegisterReq;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {
    void register(UserRegisterReq registerInfo);

    String login(UserLoginReq loginInfo, HttpServletResponse response);

    String refresh(HttpServletRequest request, HttpServletResponse response);

    void logout(HttpServletRequest request);

    void changePassword(UserChangePasswdReq changes, HttpServletRequest request);
}
