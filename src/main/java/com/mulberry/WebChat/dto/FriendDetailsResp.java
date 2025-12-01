package com.mulberry.WebChat.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FriendDetailsResp {
    private String username;
    private String nickname;
    private String status;
    private String gender;
    private String region;
    private String biography;
    private String email;
    private LocalDate birth;
    private String avatar;
    private String alias;
    private Boolean isFavor;
}
