package com.mulberry.WebChat.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserChangePasswdReq {
    @JsonIgnore
    private String username;

    @NotBlank(message = "Password not null")
    @Size(min = 6, max = 64)
    private String oldPassword;

    @NotBlank(message = "Password not null")
    @Size(min = 6, max = 64)

    private String newPassword;
    @NotBlank(message = "Password not null")
    @Size(min = 6, max = 64)
    private String confirmPassword;
}
