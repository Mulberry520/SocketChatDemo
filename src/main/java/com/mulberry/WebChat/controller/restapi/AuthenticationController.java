package com.mulberry.WebChat.controller.restapi;

import com.mulberry.WebChat.common.R;
import com.mulberry.WebChat.dto.UserLoginReq;
import com.mulberry.WebChat.dto.UserRegisterReq;
import com.mulberry.WebChat.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authService = authenticationService;
    }

    @PostMapping("/register")
    public R<String> register(@RequestBody @Valid UserRegisterReq registerInfo) {
        String errInfo = authService.register(registerInfo);
        if (errInfo != null) {
            return R.error(errInfo);
        }

        return R.success();
    }

    @PostMapping("/login")
    public R<String> login(
            @RequestBody @Valid UserLoginReq loginInfo,
            HttpServletResponse response
    ) {
        try {
            String accessToken = authService.login(loginInfo, response);
            return R.success(accessToken);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/refresh")
    public R<String> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String newAccessToken = authService.refresh(request, response);
            return R.success(newAccessToken);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }

    @GetMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        String logoutError = authService.logout(request);
        if (logoutError != null) {
            return R.error(logoutError);
        }
        return R.success("Logout success");
    }
}
