package com.mulberry.WebChat.service;

import com.mulberry.WebChat.dto.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface FriendshipService {
    List<FriendListResp> getFriends(UserDetails userDetails);

    FriendDetailsResp getFriendDetail(UserDetails userDetails, String friendUsername);

    void updateFriendInfo(UserDetails userDetails, FriendUpdateReq friendUpdateInfo);

    void deleteFriend(UserDetails userDetails, String friendUsername);

    List<RequestReceivedResp> getFriendRequestsReceived(UserDetails userDetails);

    void handleFriendRequestReceived(UserDetails userDetails, RequestHandleReq handleRequest);

    List<RequestSentResp> getFriendRequestsSent(UserDetails userDetails);

    void sendFriendRequest(UserDetails userDetails, RequestSendReq sendRequest);

    void deleteFriendRequestSent(UserDetails userDetails, Long requestId);
}
