package com.mulberry.WebChat.service;

import com.mulberry.WebChat.dto.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface ChatRoomService {
    List<RoomListResp> getJoinedRooms(UserDetails userDetails);

    RoomDetailResp getRoomDetail(UserDetails userDetails, String roomName);

    void updateRoomInfo(UserDetails userDetails, RoomAliasUpdateReq updates);

    void quitRoom(UserDetails userDetails, String roomName);

    void createRoom(UserDetails userDetails, RoomCreatedDTO createInfo);

    List<RoomRequestReceivedResp> getRoomRequestReceived(UserDetails userDetails);

    void handleRoomRequestReceived(UserDetails userDetails, RoomRequestHandleReq handleRequest);

    List<RoomRequestSentResp> getRoomRequestSent(UserDetails userDetails);

    void sendRoomRequest(UserDetails userDetails, RoomRequestSendReq roomRequest);

    void deleteRoomRequestSent(UserDetails userDetails, Long requestId);

    List<RoomCreatedDTO> getCreatedRooms(UserDetails userDetails);

    void deleteRoomMember(UserDetails userDetails, RoomMemberReq deleteMembers);
}
