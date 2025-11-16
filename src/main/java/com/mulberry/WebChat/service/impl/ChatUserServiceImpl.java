package com.mulberry.WebChat.service.impl;

import com.mulberry.WebChat.dto.UserRegisterReq;
import com.mulberry.WebChat.mapper.ChatUserMapper;
import com.mulberry.WebChat.service.ChatUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ChatUserServiceImpl implements ChatUserService {
    private final ChatUserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public ChatUserServiceImpl(
            ChatUserMapper mapper,
            PasswordEncoder passwordEncoder
    ) {
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String register(UserRegisterReq registerInfo) {
        String username = registerInfo.getUsername();
        String password = registerInfo.getPassword();
        String nickname = registerInfo.getNickname();

        if (getUerId(username) != null) {
            return "Username exists";
        }

        String encodedPasswd = passwordEncoder.encode(password);
        int affected = mapper.insertBasicInfo(username, encodedPasswd, nickname);
        if (affected == 0) {
            return "Create user failed";
        }

        return null;
    }

    @Override
    public String login(UserRegisterReq loginInfo) {
        String username = loginInfo.getUsername();
        String password = loginInfo.getPassword();
        Long userId = getUerId(username);

        if (userId == null) {
            return "Wrong username";
        }
        if (passwordEncoder.matches(password, getPasswd(userId))) {
            return "Wrong password";
        }

        return null;
    }

    @Override
    public Long getUerId(String username) {
        return mapper.selectIdByName(username);
    }

    @Override
    public String getPasswd(Long userId) {
        return mapper.selectPasswdById(userId);
    }
}
