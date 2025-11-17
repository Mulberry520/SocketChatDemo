package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.UserRegisterReq;
import com.mulberry.WebChat.service.ChatUserService;
import com.mulberry.WebChat.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final ChatUserService userService;
    private final JwtUtil jwtUtil;

    public AuthenticationController(
            ChatUserService userService,
            JwtUtil jwtUtil
    ) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public R<String> register(@RequestBody @Valid UserRegisterReq registerInfo) {
        String errInfo = userService.register(registerInfo);
        if (errInfo != null) {
            return R.error(errInfo);
        }

        return R.success();
    }

    @PostMapping("/login")
    public R<String> login(@RequestBody @Valid UserRegisterReq loginInfo) {
        String errInfo = userService.login(loginInfo);
        if (errInfo != null) {
            return R.error(errInfo);
        }

        String token = jwtUtil.generateAccessToken(loginInfo.getUsername());
        return R.success(token);
    }


}
