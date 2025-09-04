package com.example.spotifyclone.dto.auth;

import com.example.spotifyclone.dto.image.ImageDto;
import com.example.spotifyclone.entity.Idea;
import com.example.spotifyclone.entity.Image;
import com.example.spotifyclone.enums.ROLE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class CurrentUserDto {
    private Long id;
    private String name;
    private String email;
    private ROLE role;
    private boolean isVerified;

    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDate dateOfBirth;

    private List<Long> ideas;
    //private List<String> ideas1;
    private ImageDto avatarId;
    private boolean twoFactorEmail;
}
