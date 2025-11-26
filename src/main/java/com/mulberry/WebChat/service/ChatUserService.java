package com.mulberry.WebChat.service;

import com.mulberry.WebChat.dto.UserDetailDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ChatUserService {
    enum Status {
        ONLINE("ONLINE"),
        OFFLINE("OFFLINE"),
        BANNED("BANNED");

        private final String status;
        Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return this.status;
        }
    }

    UserDetailDTO getUserInfo(UserDetails userDetail);

    void updateUserInfo(UserDetailDTO updates);

    String getUserAvatar(UserDetails userDetails);

    void updateUserAvatar(MultipartFile avatarFile, UserDetails userDetails) throws IOException;
}
