package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.RequestHandleReq;
import com.mulberry.WebChat.dto.RequestReceivedResp;
import com.mulberry.WebChat.dto.RequestSendReq;
import com.mulberry.WebChat.dto.RequestSentResp;
import com.mulberry.WebChat.service.FriendshipService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class FriendRequestController {
    private final FriendshipService friendService;

    public FriendRequestController(FriendshipService friendService) {
        this.friendService = friendService;
    }

    @GetMapping("/receive")
    public R<List<RequestReceivedResp>> getReceivedRequest(@AuthenticationPrincipal UserDetails userDetails) {
        List<RequestReceivedResp> receivedRequests = friendService.getFriendRequestsReceived(userDetails);
        return R.success(receivedRequests);
    }

    @PostMapping("/receive")
    public R<Void> handleReceivedRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid RequestHandleReq handleRequest
    ) {
        friendService.handleFriendRequestReceived(userDetails, handleRequest);
        return R.success();
    }

    @GetMapping("/send")
    public R<List<RequestSentResp>> getSentRequest(@AuthenticationPrincipal UserDetails userDetails) {
        List<RequestSentResp> sentRequests = friendService.getFriendRequestsSent(userDetails);
        return R.success(sentRequests);
    }

    @PostMapping("/send")
    public R<Void> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid RequestSendReq request
    ) {
        friendService.sendFriendRequest(userDetails, request);
        return R.success();
    }

    @DeleteMapping("/send")
    public R<Void> deleteRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("id") Long requestId
    ) {
        friendService.deleteFriendRequestSent(userDetails, requestId);
        return R.success();
    }
}
