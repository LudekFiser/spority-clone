package com.example.spotifyclone.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest_email_twoFA {

    @NotNull(message = "User is required")
    private Long userId;

    @NotBlank(message = "OTP is required")
    private String otp;
}
