package com.mulberry.WebChat.entity;

import java.time.LocalDateTime;

public class RoomRequest {
    private Long id;
    private String requestUser;
    private String targetRoom;
    private String information;
    private String status;
    private LocalDateTime createTime;
}
