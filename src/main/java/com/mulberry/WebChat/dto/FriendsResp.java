package com.mulberry.WebChat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class FriendsResp {
    private String friendUsername;
    private String friendStatus;
    @JsonIgnore
    private Long friendId;
    private String alias;
    private Integer favor;
}
