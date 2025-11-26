package com.mulberry.WebChat.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Friendship {
    private Long id;
    private Long userId;
    private Long friendId;
    private String alias;
    private Integer favor;
    private String status;
    private String information;
    private LocalDateTime createTime;
}
