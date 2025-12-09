package com.mulberry.WebChat.service.impl;

import com.mulberry.WebChat.common.CommonConst;
import com.mulberry.WebChat.dto.*;
import com.mulberry.WebChat.exception.BusinessException;
import com.mulberry.WebChat.mapper.ChatUserMapper;
import com.mulberry.WebChat.mapper.FriendRequestMapper;
import com.mulberry.WebChat.mapper.FriendshipMapper;
import com.mulberry.WebChat.service.FriendshipService;
import com.mulberry.WebChat.util.FileLoadUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendshipServiceImpl implements FriendshipService {
    private final ChatUserMapper userMapper;
    private final FriendshipMapper friendMapper;
    private final FriendRequestMapper requestMapper;
    private final FileLoadUtil fileLoadUtil;

    public FriendshipServiceImpl(
            ChatUserMapper userMapper,
            FriendshipMapper friendMapper,
            FriendRequestMapper requestMapper,
            FileLoadUtil fileLoadUtil
    ) {
        this.userMapper = userMapper;
        this.friendMapper = friendMapper;
        this.requestMapper = requestMapper;
        this.fileLoadUtil = fileLoadUtil;
    }

    @Override
    public List<FriendListResp> getFriends(UserDetails userDetails) {
        List<FriendListResp> friends = friendMapper.selectFriendNames(userDetails.getUsername());
        for (FriendListResp friend : friends) {
            if (friend.getAlias() == null) {
                friend.setAlias(friend.getFriendUsername());
            }
            String avatar = userMapper.selectAvatarByName(friend.getFriendUsername());
            if (avatar != null) {
                friend.setAvatar(fileLoadUtil.generateSignedUrl(avatar));
            }
        }
        return friends;
    }

    @Override
    public void updateFriendInfo(UserDetails userDetails, FriendUpdateReq friendUpdateInfo) {
        String username = userDetails.getUsername();
        String friendUsername = friendUpdateInfo.getFriendUsername();
        if (friendMapper.selectIfFriendExists(username, friendUsername) == null) {
            throw new BusinessException("Not your friend");
        }

        String newAlias = friendUpdateInfo.getAlias();
        Boolean isFavor = friendUpdateInfo.getIsFavor();
        if (newAlias != null) {
            friendMapper.updateFriendAlias(username, friendUsername, newAlias);
        }
        if (isFavor != null) {
            friendMapper.updateFriendFavor(username, friendUsername, (isFavor) ? 1 : 0);
        }
    }

    @Override
    public void deleteFriend(UserDetails userDetails, String friendUsername) {
        String username = userDetails.getUsername();
        if (friendMapper.selectIfFriendExists(username, friendUsername) == null) {
            throw new BusinessException("Not your friend");
        }
        friendMapper.deleteFriendshipByName(username, friendUsername);
        friendMapper.deleteFriendshipByName(friendUsername, username);
    }

    @Override
    public FriendDetailsResp getFriendDetail(UserDetails userDetails, String friendUsername) {
        String username = userDetails.getUsername();
        if (friendMapper.selectIfFriendExists(username, friendUsername) == null) {
            throw new BusinessException("Not your friend");
        }

        FriendDetailsResp friendInfo = userMapper.selectByFriend(friendUsername);
        String friendAvatar = friendInfo.getAvatar();
        if (friendAvatar != null) {
            friendInfo.setAvatar(fileLoadUtil.generateSignedUrl(friendAvatar));
        }
        String alias = friendMapper.selectFriendAlias(username, friendUsername);
        friendInfo.setAlias((alias == null) ? friendUsername : alias);
        Integer isFavor = friendMapper.selectFriendIsFavor(username, friendUsername);
        friendInfo.setIsFavor(isFavor == 1);

        return friendInfo;
    }

    @Override
    public List<RequestReceivedResp> getFriendRequestsReceived(UserDetails userDetails) {
        return requestMapper.selectRequestsReceived(userDetails.getUsername());
    }

    @Override
    public List<RequestSentResp> getFriendRequestsSent(UserDetails userDetails) {
        return requestMapper.selectRequestsSent(userDetails.getUsername());
    }

    @Override
    public void sendFriendRequest(UserDetails userDetails, RequestSendReq sendRequest) {
        String username = userDetails.getUsername();
        String targetName = sendRequest.getTargetUser();
        if (friendMapper.selectIfFriendExists(username, targetName) != null) {
            throw new BusinessException("Friend already exists");
        }
        if (userMapper.selectIdByName(targetName) == null) {
            throw new BusinessException("Target user don't exists");
        }

        requestMapper.insertRequest(
                username,
                targetName,
                sendRequest.getInformation(),
                CommonConst.REQUEST_UNVERIFIED
        );
    }

    @Override
    public void handleFriendRequestReceived(UserDetails userDetails, RequestHandleReq handleRequest) {
        String username = userDetails.getUsername();
        String friendUsername = handleRequest.getRequestUser();
        if (friendMapper.selectIfFriendExists(username, friendUsername) != null) {
            throw new BusinessException("Friend already exists");
        }

        if (handleRequest.getIsApprove()) {
            friendMapper.insertFriendship(username, friendUsername);
            friendMapper.insertFriendship(friendUsername, username);
            requestMapper.updateRequestStatus(username, friendUsername, CommonConst.REQUEST_APPROVED);
            return;
        }
        requestMapper.updateRequestStatus(username, friendUsername, CommonConst.REQUEST_REJECTED);
    }

    @Override
    public void deleteFriendRequestSent(UserDetails userDetails, Long requestId) {
        requestMapper.deleteRequestSentById(requestId, userDetails.getUsername());
    }
}
