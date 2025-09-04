package com.example.spotifyclone.dto.auth;

import com.example.spotifyclone.dto.image.ImageDto;
import com.example.spotifyclone.dto.image.UploadedImageDto;
import com.example.spotifyclone.entity.Idea;
import com.example.spotifyclone.entity.Image;
import com.example.spotifyclone.enums.ROLE;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


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

    private List<Long> ideas;
    private Image avatarId;
    private boolean twoFactorEmail;

}
