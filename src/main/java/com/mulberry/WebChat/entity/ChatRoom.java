package com.mulberry.WebChat.entity;

import java.time.LocalDateTime;

public class ChatRoom {
    private Long id;
    private String roomName;
    private String createUser;
    private Integer isPublic;
    private String information;
    private String avatar;
    private LocalDateTime createTime;
}
