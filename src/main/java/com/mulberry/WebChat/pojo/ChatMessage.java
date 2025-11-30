package com.mulberry.WebChat.pojo;

import lombok.Data;

@Data
public class ChatMessage {
    private String content;
    private String sender;
    private String recipient;
    private Long timestamp;

    public ChatMessage() {
    }

    public ChatMessage(String content, String sender, Long timestamp) {
        this.content = content;
        this.sender = sender;
        this.recipient = null;
        this.timestamp = timestamp;
    }
}
