package com.mulberry.WebChat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FriendUpdateReq {
    @NotNull
    private String friendUsername;
    @Size(max = 32)
    private String alias;
    private Boolean isFavor;
}
