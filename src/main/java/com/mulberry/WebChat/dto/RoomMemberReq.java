package com.mulberry.WebChat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoomMemberReq {
    @NotNull
    @Size(max = 32)
    private String roomName;
    private List<String> members;
}
