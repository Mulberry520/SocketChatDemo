package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.UserDetailDTO;
import com.mulberry.WebChat.service.ChatUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final ChatUserService userService;

    public UserController(ChatUserService userService) {
        this.userService = userService;
    }


    @GetMapping("/userinfo")
    public R<UserDetailDTO> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        UserDetailDTO userInfo = userService.getUserInfo(userDetails);
        if (userInfo != null) {
            return R.success(userInfo);
        }
        return R.error();
    }

    @PostMapping("/userinfo")
    public R<Void> updateUserInfo(
            @RequestBody @Valid UserDetailDTO updates,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        updates.setUsername(userDetails.getUsername());
        if (userService.updateUserInfo(updates)) {
            return R.success();
        }
        return R.error();
    }
}
