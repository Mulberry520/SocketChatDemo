package com.mulberry.WebChat.service;

import com.mulberry.WebChat.dto.UserDetailDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ChatUserService {
    UserDetailDTO getUserInfo(UserDetails userDetail);

    void updateUserInfo(UserDetailDTO updates);

    String getUserAvatar(UserDetails userDetails);

    void updateUserAvatar(MultipartFile avatarFile, UserDetails userDetails) throws IOException;
}
