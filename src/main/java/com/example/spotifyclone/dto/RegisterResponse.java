package com.example.spotifyclone.dto;

import com.example.spotifyclone.enums.ROLE;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@Builder
public class RegisterResponse {

    private Long id;
    private String email;
    private String name;
    private ROLE role;
    private boolean isVerified;

    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDate dateOfBirth;

}
