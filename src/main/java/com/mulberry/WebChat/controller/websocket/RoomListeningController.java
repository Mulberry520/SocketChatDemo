package com.mulberry.WebChat.controller.websocket;

import com.mulberry.WebChat.common.CommonConst;
import com.mulberry.WebChat.mapper.ChatRoomMapper;
import com.mulberry.WebChat.mapper.RoomUserMapper;
import com.mulberry.WebChat.pojo.ChatMessage;
import com.mulberry.WebChat.service.UserListeningRoomService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Set;

@Controller
public class RoomListeningController {
    private final RoomUserMapper roomUserMapper;
    private final ChatRoomMapper chatRoomMapper;
    private final UserListeningRoomService listeningRoomService;
    private final SimpMessagingTemplate messagingTemplate;

    public RoomListeningController(
            RoomUserMapper roomUserMapper,
            UserListeningRoomService listeningRoomService,
            ChatRoomMapper chatRoomMapper,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.roomUserMapper = roomUserMapper;
        this.listeningRoomService = listeningRoomService;
        this.chatRoomMapper = chatRoomMapper;
        this.messagingTemplate = messagingTemplate;
    }


    @MessageMapping("/room/{roomName}/connect")
    public void connectRoom(
            @DestinationVariable String roomName,
            SimpMessageHeaderAccessor accessor
    ) {
        String username = getCurrentUser(accessor);
        if (username == null) {
            return;
        }
        if (chatRoomMapper.selectIsRoomExists(roomName) == null) {
            return;
        }
        if (roomUserMapper.selectIfUserInRoom(username, roomName) == null) {
            return;
        }

        listeningRoomService.connectRoom(username, roomName);
        ChatMessage joinMessage = new ChatMessage(CommonConst.MESSAGE_TYPE_JOIN, roomName, username);
        broadMessage(joinMessage);
    }

    @MessageMapping("/room/{roomName}/disconnect")
    public void disconnectRoom(
            @DestinationVariable String roomName,
            SimpMessageHeaderAccessor accessor
    ) {
        String username = getCurrentUser(accessor);
        if (username == null) {
            return;
        }
        if (chatRoomMapper.selectIsRoomExists(roomName) == null) {
            return;
        }
        listeningRoomService.disconnectRoom(username, roomName);
        ChatMessage quitMessage = new ChatMessage(CommonConst.MESSAGE_TYPE_QUIT, roomName, username);
        broadMessage(quitMessage);
    }

    @MessageMapping("/room/{roomName}/chat")
    public void handleChat(
            @DestinationVariable String roomName,
            SimpMessageHeaderAccessor accessor,
            ChatMessage message
    ) {
        String username = getCurrentUser(accessor);
        if (chatRoomMapper.selectIsRoomExists(roomName) == null) {
            return;
        }
        if (username == null) {
            return;
        }
        if (roomUserMapper.selectIfUserInRoom(username, roomName) == null) {
            return;
        }
        if (!listeningRoomService.isListening(username, roomName)) {
            return;
        }

        message.setType(CommonConst.MESSAGE_TYPE_MESSAGE);
        message.setSender(username);
        message.setRecipient(roomName);
        message.setTime(LocalDateTime.now());

        broadMessage(message);
    }

    private void broadMessage(ChatMessage message) {
        Set<String> listeners = listeningRoomService.getListeners(message.getRecipient());
        for (String user : listeners) {
            messagingTemplate.convertAndSendToUser(user, "/queue/room-message", message);
        }
    }

    private String getCurrentUser(SimpMessageHeaderAccessor accessor) {
        return (String) accessor.getSessionAttributes().get("username");
    }
}
