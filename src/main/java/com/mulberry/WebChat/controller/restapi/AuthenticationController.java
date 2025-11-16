package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.UserRegisterReq;
import com.mulberry.WebChat.service.ChatUserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final ChatUserService userService;

    public AuthenticationController(ChatUserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public R<Void> register(@RequestBody @Valid UserRegisterReq registerInfo) {
        String errInfo = userService.register(registerInfo);
        if (errInfo != null) {
            return R.error(errInfo);
        }

        return R.success();
    }

    @PostMapping("/login")
    public R<Void> login(@RequestBody @Valid UserRegisterReq loginInfo) {
        String errInfo = userService.login(loginInfo);
        if (errInfo != null) {
            return R.error(errInfo);
        }

        return R.success();
    }
}
