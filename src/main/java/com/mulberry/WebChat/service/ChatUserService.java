package com.mulberry.WebChat.service;

import com.mulberry.WebChat.dto.UserRegisterReq;

public interface ChatUserService {
    String register(UserRegisterReq registerInfo);

    String login(UserRegisterReq loginInfo);

    Long getUerId(String username);

    String getPasswd(Long userId);
}
