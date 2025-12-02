package com.mulberry.WebChat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoomCreatedDTO {
    @NotBlank(message = "Username not null")
    @Pattern(regexp = "^[0-9a-zA-Z_]+$", message = "Username only numbers, letters or underscores")
    @Size(min = 6, max = 32)
    private String roomName;
    @NotNull
    private Boolean isPublic;
    @Size(max = 255)
    private String information;
}
