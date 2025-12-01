package com.mulberry.WebChat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestSentResp {
    private Long id;
    private String targetUser;
    private String information;
    private String status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;
}
