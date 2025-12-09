package com.mulberry.WebChat.dto;

import lombok.Data;

@Data
public class RoomListResp {
    private String roomName;
    private String roomAlias;
    private String userAlias;
    private String avatar;
}
