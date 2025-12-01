package com.mulberry.WebChat.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendRequest {
    private Long id;
    private String requestUser;
    private String targetUser;
    private String information;
    private String status;
    private LocalDateTime createTime;
}
