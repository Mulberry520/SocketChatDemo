package com.mulberry.WebChat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendRequestDTO {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @JsonIgnore
    private Long userId;

    @JsonIgnore
    private String username;

    @JsonIgnore
    private Long friendId;

    @NotBlank(message = "Username not null")
    @Pattern(regexp = "^[0-9a-zA-Z_]+$", message = "Username only numbers, letters or underscores")
    @Size(min = 6, max = 32)
    private String friendUsername;

    @Size(max = 32)
    private String alias;

    @Size(max = 255)
    private String information;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;
}
