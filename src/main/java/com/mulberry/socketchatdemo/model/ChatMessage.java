package com.mulberry.socketchatdemo.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChatMessage {
    private String content;
    private String sender;
    private MessageType type;

    public ChatMessage() {
    }

    public ChatMessage(String content, String sender, MessageType type) {
        this.content = content;
        this.sender = sender;
        this.type = type;
    }

    public enum MessageType {
        JOIN,
        CHAT,
        LEAVE
    }
}
