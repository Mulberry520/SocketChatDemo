package com.mulberry.WebChat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendResponseResp {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    @JsonIgnore
    private Long userId;
    @JsonIgnore
    private Long friendId;

    private String requestUsername;
    private String requestNickname;
    private String information;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;
}
