package com.mulberry.WebChat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoomRequestSendReq {
    @NotNull
    @Size(max = 32)
    private String targetRoom;
    private String information;
}
