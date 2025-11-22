package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.UserChangePasswdReq;
import com.mulberry.WebChat.dto.UserLoginReq;
import com.mulberry.WebChat.dto.UserRegisterReq;
import com.mulberry.WebChat.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authService = authenticationService;
    }

    @PostMapping("/register")
    public R<Void> register(@RequestBody @Valid UserRegisterReq registerInfo) {
        authService.register(registerInfo);
        return R.success();
    }

    @PostMapping("/login")
    public R<String> login(
            @RequestBody @Valid UserLoginReq loginInfo,
            HttpServletResponse response
    ) {
        String accessToken = authService.login(loginInfo, response);
        return R.success(accessToken);
    }

    @GetMapping("/refresh")
    public R<String> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String newAccessToken = authService.refresh(request, response);
        return R.success(newAccessToken);
    }

    @GetMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return R.success();
    }

    @PostMapping("/password")
    public R<Void> changePassword(
            @RequestBody @Valid UserChangePasswdReq change,
            @AuthenticationPrincipal UserDetails userDetail,
            HttpServletRequest request
    ) {
        change.setUsername(userDetail.getUsername());
        authService.changePassword(change, request);
        return R.success();
    }
}