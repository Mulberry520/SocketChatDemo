package com.mulberry.WebChat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoomDetailResp {
    private String roomName;
    private String roomAlias;
    private String userAlias;
    private String createUser;
    private Boolean isPublic;
    private String information;
    private String avatar;
    private List<String> members;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;
}
