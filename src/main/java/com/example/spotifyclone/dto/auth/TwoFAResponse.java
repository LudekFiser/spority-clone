package com.example.spotifyclone.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TwoFAResponse {

    private String message;
    private Long userId;
}
