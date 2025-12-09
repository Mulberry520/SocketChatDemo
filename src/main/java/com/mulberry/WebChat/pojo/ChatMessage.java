package com.mulberry.WebChat.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private String type;
    private String content;
    private String sender;
    private String recipient;
    private LocalDateTime time;

    public ChatMessage() {
    }
}
