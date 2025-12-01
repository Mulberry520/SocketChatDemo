package com.mulberry.WebChat.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestSendReq {
    @NotNull
    private String targetUser;
    @Size(max = 255)
    private String information;
}
