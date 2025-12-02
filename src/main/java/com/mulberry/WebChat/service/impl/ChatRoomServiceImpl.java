package com.mulberry.WebChat.service.impl;

import com.mulberry.WebChat.common.CommonConst;
import com.mulberry.WebChat.dto.*;
import com.mulberry.WebChat.exception.BusinessException;
import com.mulberry.WebChat.mapper.ChatRoomMapper;
import com.mulberry.WebChat.mapper.RoomRequestMapper;
import com.mulberry.WebChat.mapper.RoomUserMapper;
import com.mulberry.WebChat.service.ChatRoomService;
import com.mulberry.WebChat.util.FileLoadUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatRoomServiceImpl implements ChatRoomService {
    private final RoomUserMapper roomUserMapper;
    private final ChatRoomMapper chatRoomMapper;
    private final RoomRequestMapper requestMapper;
    private final FileLoadUtil fileLoadUtil;

    public ChatRoomServiceImpl(
            RoomUserMapper roomUserMapper,
            ChatRoomMapper chatRoomMapper,
            FileLoadUtil fileLoadUtil,
            RoomRequestMapper requestMapper
    ) {
        this.roomUserMapper = roomUserMapper;
        this.chatRoomMapper = chatRoomMapper;
        this.fileLoadUtil = fileLoadUtil;
        this.requestMapper = requestMapper;
    }

    @Override
    public List<RoomListResp> getJoinedRooms(UserDetails userDetails) {
        String username = userDetails.getUsername();
        List<RoomListResp> rooms = roomUserMapper.selectJoinedRooms(username);
        for (RoomListResp room : rooms) {
            if (room.getRoomAlias() == null) {
                room.setRoomAlias(room.getRoomName());
            }
            if (room.getUserAlias() == null) {
                room.setUserAlias(username);
            }
        }

        return rooms;
    }

    @Override
    public RoomDetailResp getRoomDetail(UserDetails userDetails, String roomName) {
        String username = userDetails.getUsername();
        if (roomUserMapper.selectIfUserInRoom(username, roomName) == null) {
            throw new BusinessException("You not in this room");
        }

        RoomDetailResp roomDetail = chatRoomMapper.selectRoomDetail(roomName);
        roomDetail.setMembers(roomUserMapper.selectUsersByRoom(roomName));
        String avatar = roomDetail.getAvatar();
        if (avatar != null) {
            roomDetail.setAvatar(fileLoadUtil.generateSignedUrl(avatar));
        }
        RoomListResp alias = roomUserMapper.selectAlias(username, roomName);
        roomDetail.setRoomAlias(alias.getRoomAlias());
        roomDetail.setUserAlias(alias.getUserAlias());

        return roomDetail;
    }

    @Override
    public void updateRoomInfo(UserDetails userDetails, RoomAliasUpdateReq updates) {
        String username = userDetails.getUsername();
        String roomName = updates.getRoomName();
        if (roomUserMapper.selectIfUserInRoom(username, roomName) == null) {
            throw new BusinessException("You are not in this room");
        }

        RoomListResp oldAlias = roomUserMapper.selectAlias(username, roomName);
        String roomAlias = updates.getRoomAlias();
        String userAlias = updates.getUserAlias();
        roomUserMapper.updateAlias(
                username,
                roomName,
                (userAlias != null) ? userAlias : oldAlias.getUserAlias(),
                (roomAlias != null) ? roomAlias : oldAlias.getRoomAlias()
        );
    }

    @Override
    public void quitRoom(UserDetails userDetails, String roomName) {
        String username = userDetails.getUsername();
        if (chatRoomMapper.selectIsRoomOwner(roomName, username) != null) {
            throw new BusinessException("Room owner can't quit room");
        }
        if (roomUserMapper.selectIfUserInRoom(username, roomName) == null) {
            throw new BusinessException("You are not in this room");
        }

        roomUserMapper.deleteUserInRoom(username, roomName);
    }

    @Override
    public void createRoom(UserDetails userDetails, RoomCreatedDTO createInfo) {
        String username = userDetails.getUsername();

        String roomName = createInfo.getRoomName();
        if (chatRoomMapper.selectIsRoomExists(roomName) != null) {
            throw new BusinessException("This room already exists");
        }

        chatRoomMapper.insertRoom(
                roomName,
                username,
                ((createInfo.getIsPublic()) ? 1 : 0),
                createInfo.getInformation()
        );
        roomUserMapper.insertRoomUser(username, roomName);
    }

    @Override
    public List<RoomRequestReceivedResp> getRoomRequestReceived(UserDetails userDetails) {
        return requestMapper.selectReceivedRoomRequest(userDetails.getUsername());
    }

    @Override
    public void handleRoomRequestReceived(UserDetails userDetails, RoomRequestHandleReq handleRequest) {
        String username = userDetails.getUsername();
        String requestUser = handleRequest.getRequestUser();
        String requestRoom = handleRequest.getRequestRoom();
        if (chatRoomMapper.selectIsRoomExists(requestRoom) == null) {
            throw new BusinessException("Not have this room");
        }
        if (chatRoomMapper.selectIsRoomPublic(requestRoom)) {
            requestMapper.updateRequestStatus(requestUser, requestRoom, CommonConst.REQUEST_APPROVED);
            return;
        }

        if (chatRoomMapper.selectIsRoomOwner(requestRoom, username) == null) {
            throw new BusinessException("You are not room owner");
        }
        if (roomUserMapper.selectIfUserInRoom(requestUser, requestRoom) != null) {
            requestMapper.updateRequestStatus(requestUser, requestRoom, CommonConst.REQUEST_APPROVED);
            return;
        }

        if (handleRequest.getIsApprove()) {
            roomUserMapper.insertRoomUser(requestUser, requestRoom);
            requestMapper.updateRequestStatus(requestUser, requestRoom, CommonConst.REQUEST_APPROVED);
        } else {
            requestMapper.updateRequestStatus(requestUser, requestRoom, CommonConst.REQUEST_REJECTED);
        }

    }

    @Override
    public List<RoomRequestSentResp> getRoomRequestSent(UserDetails userDetails) {
        return requestMapper.selectSentRoomRequest(userDetails.getUsername());
    }

    @Override
    public void sendRoomRequest(UserDetails userDetails, RoomRequestSendReq roomRequest) {
        String username = userDetails.getUsername();
        String targetRoom = roomRequest.getTargetRoom();
        if (chatRoomMapper.selectIsRoomExists(targetRoom) == null) {
            throw new BusinessException("Not have this room");
        }
        if (chatRoomMapper.selectIsRoomOwner(targetRoom, username) != null) {
            throw new BusinessException("You are the room owner");
        }
        if (roomUserMapper.selectIfUserInRoom(username, targetRoom) != null) {
            throw new BusinessException("You already in this room");
        }

        requestMapper.insertRequest(
                username,
                targetRoom,
                roomRequest.getInformation(),
                CommonConst.REQUEST_UNVERIFIED
        );
        if (chatRoomMapper.selectIsRoomPublic(targetRoom)) {
            roomUserMapper.insertRoomUser(username, targetRoom);
            requestMapper.updateRequestStatus(username, targetRoom, CommonConst.REQUEST_APPROVED);
        }
    }

    @Override
    public void deleteRoomRequestSent(UserDetails userDetails, Long requestId) {
        requestMapper.deleteRequest(userDetails.getUsername(), requestId);
    }

    @Override
    public List<RoomCreatedDTO> getCreatedRooms(UserDetails userDetails) {
        return chatRoomMapper.selectRoomsByCreator(userDetails.getUsername());
    }

    @Override
    public void deleteRoomMember(UserDetails userDetails, RoomMemberReq deleteMembers) {
        String username = userDetails.getUsername();
        String roomName = deleteMembers.getRoomName();
        if (chatRoomMapper.selectIsRoomOwner(roomName, username) == null) {
            throw new BusinessException("You are not the room owner");
        }

        for (String member : deleteMembers.getMembers()) {
            roomUserMapper.deleteUserInRoom(member, roomName);
        }
    }
}
