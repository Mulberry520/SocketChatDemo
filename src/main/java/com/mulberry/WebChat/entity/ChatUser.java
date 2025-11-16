package com.mulberry.WebChat.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ChatUser {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String status;
    private String avatar;
    private String gender;
    private String region;
    private LocalDate birth;
    private String biography;
    private String phone;
    private String email;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
