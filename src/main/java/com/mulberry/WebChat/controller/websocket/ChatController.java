package com.mulberry.WebChat.controller.websocket;

import com.mulberry.WebChat.pojo.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat")
    @SendTo("/topic/message")
    public ChatMessage handleGroupChat(
            ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        message.setSender(username);
        return message;
    }

    @MessageMapping("/private")
    public void handlePrivateChat(
            ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String sender = (String) headerAccessor.getSessionAttributes().get("username");
        String recipient = message.getRecipient();

        message.setSender(sender);

        messagingTemplate.convertAndSendToUser(
                recipient,
                "/queue/private",
                message
        );
    }

    @MessageMapping("/room/{roomId}/message")
    @SendTo("/topic/room/{roomId}")
    public ChatMessage handleRoomChat(
            @DestinationVariable("roomId") String roomId,
            ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String sender = (String) headerAccessor.getSessionAttributes().get("username");
        message.setSender(sender);
        return message;
    }
}
