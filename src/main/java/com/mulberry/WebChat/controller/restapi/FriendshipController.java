package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.FriendRequestDTO;
import com.mulberry.WebChat.dto.FriendResponseResp;
import com.mulberry.WebChat.dto.FriendsResp;
import com.mulberry.WebChat.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {
    private final FriendshipService friendService;

    public FriendshipController(FriendshipService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public R<List<FriendsResp>> getFriends(@AuthenticationPrincipal UserDetails userDetails) {
        List<FriendsResp> friends = friendService.getFriends(userDetails);
        return R.success(friends);
    }

    @GetMapping("/request")
    public R<List<FriendRequestDTO>> getRequests(@AuthenticationPrincipal UserDetails userDetails) {
        List<FriendRequestDTO> requestList = friendService.getFriendRequest(userDetails);
        return R.success(requestList);
    }

    @PostMapping("/request")
    public R<Void> requestFriend(
            @RequestBody @Valid FriendRequestDTO friendRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        friendRequest.setUsername(userDetails.getUsername());
        friendService.requestFriend(friendRequest);
        return R.success();
    }

    @GetMapping("/response")
    public R<List<FriendResponseResp>> getResponse(@AuthenticationPrincipal UserDetails userDetails) {
        List<FriendResponseResp> responses = friendService.getResponseRequest(userDetails);
        return R.success(responses);
    }

    @PostMapping("/response/{id}")
    public R<Void> handleFriendRequest(
            @PathVariable("id") Long friendRequestId,
            @RequestParam("operation") String operation,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        friendService.responseFriendRequest(userDetails, friendRequestId, operation);
        return R.success();
    }

    @DeleteMapping("/{id}")
    public R<Void> deleteFriendship(
            @PathVariable("id") Long friendshipId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        friendService.deleteFriendship(userDetails, friendshipId);
        return R.success();
    }
}
