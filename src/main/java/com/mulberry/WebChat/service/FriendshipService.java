package com.mulberry.WebChat.service;

import com.mulberry.WebChat.dto.FriendRequestDTO;
import com.mulberry.WebChat.dto.FriendResponseResp;
import com.mulberry.WebChat.dto.FriendsResp;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface FriendshipService {
    enum Status {
        APPROVED("approved"),
        REJECTED("rejected"),
        BLOCKED("blocked"),
        UNVERIFIED("unverified");

        private final String status;
        Status(String status) {
            this.status = status;
        }

        public String getStatus() {
            return this.status;
        }
    }

    List<FriendsResp> getFriends(UserDetails userDetails);

    void requestFriend(FriendRequestDTO friendRequest);

    List<FriendRequestDTO> getFriendRequest(UserDetails userDetails);

    void deleteFriendship(UserDetails userDetails, Long friendshipId);

    List<FriendResponseResp> getResponseRequest(UserDetails userDetails);

    void responseFriendRequest(UserDetails userDetails, Long friendshipId, String operation);
}
