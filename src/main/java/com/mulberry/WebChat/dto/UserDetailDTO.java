package com.mulberry.WebChat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class UserDetailDTO {
    @JsonIgnore
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String username;

    @Size(max = 32)
    private String nickname;

    @Size(max = 16)
    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;

    @Size(max = 128)
    private String region;

    @Size(max = 255)
    private String biography;

    @Size(max = 64)
    @Email(message = "Email format inaccurate")
    private String email;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createTime;
}
