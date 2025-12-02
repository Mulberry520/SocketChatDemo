package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.RoomAliasUpdateReq;
import com.mulberry.WebChat.dto.RoomDetailResp;
import com.mulberry.WebChat.dto.RoomListResp;
import com.mulberry.WebChat.service.ChatRoomService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomJoinedController {
    private final ChatRoomService roomService;

    public RoomJoinedController(ChatRoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/joined")
    public R<List<RoomListResp>> getJoinedRooms(@AuthenticationPrincipal UserDetails userDetails) {
        List<RoomListResp> joinedRooms = roomService.getJoinedRooms(userDetails);
        return R.success(joinedRooms);
    }

    @GetMapping
    public R<RoomDetailResp> getRoomDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("roomName") String roomName
    ) {
        RoomDetailResp roomDetails = roomService.getRoomDetail(userDetails, roomName);
        return R.success(roomDetails);
    }

    @PostMapping
    public R<Void> updateRoomAlias(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid RoomAliasUpdateReq updates
    ) {
        roomService.updateRoomInfo(userDetails, updates);
        return R.success();
    }

    @DeleteMapping
    public R<Void> quiteRoom(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("roomName") String roomName
    ) {
        roomService.quitRoom(userDetails, roomName);
        return R.success();
    }
}
