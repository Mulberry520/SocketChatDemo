package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.*;
import com.mulberry.WebChat.service.ChatRoomService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms/manager")
public class RoomManagerController {
    private final ChatRoomService roomService;

    public RoomManagerController(ChatRoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public R<List<RoomCreatedDTO>> getCreatedRooms(@AuthenticationPrincipal UserDetails userDetails) {
        List<RoomCreatedDTO> createdRooms = roomService.getCreatedRooms(userDetails);
        return R.success(createdRooms);
    }

    @PostMapping
    public R<Void> createRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid RoomCreatedDTO createInfo
    ) {
        roomService.createRoom(userDetails, createInfo);
        return R.success();
    }

    @DeleteMapping("/members")
    public R<Void> deleteMembers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid RoomMemberReq members
    ) {
        roomService.deleteRoomMember(userDetails, members);
        return R.success();
    }

    @GetMapping("/receive")
    public R<List<RoomRequestReceivedResp>> getReceivedRequests(@AuthenticationPrincipal UserDetails userDetails) {
        List<RoomRequestReceivedResp> receivedRequests = roomService.getRoomRequestReceived(userDetails);
        return R.success(receivedRequests);
    }

    @PostMapping("/receive")
    public R<Void> handleReceivedRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid RoomRequestHandleReq handleRequest
    ) {
        roomService.handleRoomRequestReceived(userDetails, handleRequest);
        return R.success();
    }

    @GetMapping("/send")
    public R<List<RoomRequestSentResp>> getSentRequest(@AuthenticationPrincipal UserDetails userDetails) {
        List<RoomRequestSentResp> sentRequests = roomService.getRoomRequestSent(userDetails);
        return R.success(sentRequests);
    }

    @PostMapping("/send")
    public R<Void> sendRoomRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid RoomRequestSendReq request
    ) {
        roomService.sendRoomRequest(userDetails, request);
        return R.success();
    }

    @DeleteMapping("/send")
    public R<Void> deleteRoomRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("id") Long requestId
    ) {
        roomService.deleteRoomRequestSent(userDetails, requestId);
        return R.success();
    }
}