package com.mulberry.WebChat.service.impl;

import com.mulberry.WebChat.dto.FriendRequestDTO;
import com.mulberry.WebChat.dto.FriendResponseResp;
import com.mulberry.WebChat.dto.FriendsResp;
import com.mulberry.WebChat.exception.BusinessException;
import com.mulberry.WebChat.mapper.ChatUserMapper;
import com.mulberry.WebChat.mapper.FriendshipMapper;
import com.mulberry.WebChat.service.FriendshipService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipServiceImpl implements FriendshipService {
    private final ChatUserMapper userMapper;
    private final FriendshipMapper friendMapper;

    public FriendshipServiceImpl(
            ChatUserMapper userMapper,
            FriendshipMapper friendMapper
    ) {
        this.userMapper = userMapper;
        this.friendMapper = friendMapper;
    }

    @Override
    public List<FriendsResp> getFriends(UserDetails userDetails) {
        Long userId = userMapper.selectIdByName(userDetails.getUsername());
        List<FriendsResp> friendships = friendMapper.selectFriendById(userId);
        for (FriendsResp friend : friendships) {
            String friendUsername = userMapper.selectNameById(friend.getFriendId());
            friend.setFriendStatus(userMapper.selectStatusByName(friendUsername));
            friend.setFriendUsername(friendUsername);
        }
        return friendships;
    }

    @Override
    public void requestFriend(FriendRequestDTO friendRequest) {
        Long userId = userMapper.selectIdByName(friendRequest.getUsername());
        Long friendId = userMapper.selectIdByName(friendRequest.getFriendUsername());
        if (userId == null || friendId == null) {
            throw new BusinessException("Can't find target user");
        }
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Can't request friend with self");
        }
        if (friendMapper.selectFriendshipIfExists(userId, friendId) != null) {
            throw new IllegalArgumentException("Friendship already exists");
        }

        String alias = friendRequest.getAlias();
        if (alias == null) {
            alias = userMapper.selectNicknameById(friendId);
        }
        String information = friendRequest.getInformation();
        if (information == null || information.trim().isEmpty()) {
            information = "Hello, I'm " + friendRequest.getUsername();
        }

        friendMapper.insertFriendship(
                userId,
                friendId,
                alias,
                Status.UNVERIFIED.getStatus(),
                information
        );
    }

    @Override
    public List<FriendRequestDTO> getFriendRequest(UserDetails userDetails) {
        Long userId = userMapper.selectIdByName(userDetails.getUsername());
        List<FriendRequestDTO> friendRequests = friendMapper.selectFriendRequestByUserId(userId);
        for (FriendRequestDTO request : friendRequests) {
            request.setFriendUsername(userMapper.selectNameById(request.getFriendId()));
        }
        return friendRequests;
    }

    @Override
    public void deleteFriendship(UserDetails userDetails, Long friendshipId) {
        FriendRequestDTO friendship = friendMapper.selectFriendshipById(friendshipId);
        Long userId = friendship.getUserId();
        if (!userMapper.selectNameById(userId).equals(userDetails.getUsername())) {
            throw new BusinessException("Not your friendship");
        }

        if (friendship.getStatus().equals(Status.APPROVED.getStatus())) {
            friendMapper.updateStatusById(friendship.getFriendId(), userId, Status.UNVERIFIED.getStatus());
        }
        int affected = friendMapper.deleteFriendshipById(friendshipId);
        if (affected != 1) {
            throw new BusinessException("Delete friendship failed");
        }
    }

    @Override
    public List<FriendResponseResp> getResponseRequest(UserDetails userDetails) {
        Long userId = userMapper.selectIdByName(userDetails.getUsername());
        List<FriendResponseResp> responses = friendMapper.selectFriendshipAboutUser(userId);
        for (FriendResponseResp response : responses) {
            Long requestUserId = response.getUserId();
            response.setRequestUsername(userMapper.selectNameById(requestUserId));
            response.setRequestNickname(userMapper.selectNicknameById(requestUserId));
        }
        return responses;
    }

    @Override
    public void responseFriendRequest(UserDetails userDetails, Long friendshipId, String operation) {
        FriendRequestDTO friendship = friendMapper.selectFriendshipById(friendshipId);
        Long currUserId = friendship.getFriendId();
        Long requestFriendId = friendship.getUserId();
        if (!userMapper.selectNameById(currUserId).equals(userDetails.getUsername())) {
            throw new BusinessException("Friend request not for you");
        }

        String currStatus = friendship.getStatus();
        if (currStatus.equals(Status.APPROVED.getStatus())) {
            throw new BusinessException("You have approved this friend request");
        }

        if (operation.equals(Status.REJECTED.getStatus())) {
            friendMapper.updateStatusById(requestFriendId, currUserId, operation);
            return;
        }

        if (operation.equals(Status.APPROVED.getStatus())) {
            friendMapper.insertFriendship(
                    currUserId,
                    requestFriendId,
                    userMapper.selectNicknameById(requestFriendId),
                    operation,
                    ""
            );
            friendMapper.updateStatusById(requestFriendId, currUserId, operation);
            return;
        }

        throw new IllegalArgumentException("Wrong operation");
    }
}
