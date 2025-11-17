package com.mulberry.WebChat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginReq {
    @NotBlank(message = "Username not null")
    @Pattern(regexp = "^[0-9a-zA-Z_]+$", message = "Username only numbers, letters or underscores")
    @Size(min = 6, max = 32)
    private String username;

    @NotBlank(message = "Password not null")
    @Size(min = 6, max = 64)
    private String password;
}
