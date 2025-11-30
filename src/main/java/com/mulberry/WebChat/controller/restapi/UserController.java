package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.UserDetailDTO;
import com.mulberry.WebChat.service.ChatUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
        return R.success( userInfo);
    }

    @PostMapping("/userinfo")
    public R<Void> updateUserInfo(
            @RequestBody @Valid UserDetailDTO updates,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        updates.setUsername(userDetails.getUsername());
        userService.updateUserInfo(updates);
        return R.success();
    }

    @GetMapping("/avatar")
    public R<String> getAvatar(@AuthenticationPrincipal UserDetails userDetails) {
        String avatar = userService.getUserAvatar(userDetails);
        if (avatar == null) {
            return R.success("Haven't avatar", null);
        }
        return R.success("Avatar url is valid for 30 minutes", avatar);
    }

    @PostMapping("/avatar")
    public R<Void> updateAvatar(
            @RequestParam("file") MultipartFile avatar,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            userService.updateUserAvatar(avatar, userDetails);
            return R.success();
        } catch (IOException e) {
            return R.error("Save new avatar failed");
        }
    }
}
