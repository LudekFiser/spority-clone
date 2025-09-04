package com.example.spotifyclone.mapper;


import com.example.spotifyclone.dto.auth.CurrentUserDto;
import com.example.spotifyclone.dto.auth.RegisterRequest;
import com.example.spotifyclone.dto.auth.RegisterResponse;
import com.example.spotifyclone.dto.image.ImageDto;
import com.example.spotifyclone.dto.image.UploadedImageDto;
import com.example.spotifyclone.entity.Idea;
import com.example.spotifyclone.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .email(request.getEmail())
                .name(request.getName())
                .password(request.getPassword())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .build();
    }

    public RegisterResponse toResponse(User user) {
        return RegisterResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .createdAt(user.getCreatedAt())
                .twoFactorEmail(user.getTwoFactorEmail())
                .build();
    }

    public CurrentUserDto toUserDto(User user) {
        return CurrentUserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .dateOfBirth(user.getDateOfBirth())
                .twoFactorEmail(user.getTwoFactorEmail())
                .isVerified(user.isVerified())
                .avatarId(new ImageDto(
                        user.getAvatarId().getImageUrl(),
                        user.getAvatarId().getPublicId(),
                        user.getAvatarId().getOrd()))
                .build();
    }
}
