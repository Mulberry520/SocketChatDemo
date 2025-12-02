package com.mulberry.WebChat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoomAliasUpdateReq {
    @NotNull
    @Size(max = 32)
    private String roomName;
    @Size(max = 32)
    private String roomAlias;
    @Size(max = 32)
    private String userAlias;
}
