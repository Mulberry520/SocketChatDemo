package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.FriendDetailsResp;
import com.mulberry.WebChat.dto.FriendListResp;
import com.mulberry.WebChat.dto.FriendUpdateReq;
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

    @GetMapping("/all")
    public R<List<FriendListResp>> getFriends(@AuthenticationPrincipal UserDetails userDetails) {
        List<FriendListResp> friends = friendService.getFriends(userDetails);
        return R.success(friends);
    }

    @GetMapping
    public R<FriendDetailsResp> getFriendDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("friend") String friendUsername
    ) {
        FriendDetailsResp friendDetail = friendService.getFriendDetail(userDetails, friendUsername);
        return R.success(friendDetail);
    }

    @PostMapping
    public R<Void> updateFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid FriendUpdateReq friendUpdates
    ) {
        friendService.updateFriendInfo(userDetails, friendUpdates);
        return R.success();
    }

    @DeleteMapping
    public R<Void> deleteFriend(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("friend") String friendUsername
    ) {
        friendService.deleteFriend(userDetails, friendUsername);
        return R.success();
    }
}
