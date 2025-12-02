package com.mulberry.WebChat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoomRequestHandleReq {
    @NotNull
    @Size(max = 32)
    private String requestUser;
    @NotNull
    @Size(max = 32)
    private String requestRoom;
    @NotNull
    private Boolean isApprove;
}
