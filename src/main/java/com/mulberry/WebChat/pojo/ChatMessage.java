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

    public ChatMessage(String type, String recipient, String content) {
        this.type = type;
        this.content = content;
        this.sender = "SYSTEM";
        this.recipient = recipient;
        this.time = LocalDateTime.now();
    }
}
