package com.example.spotifyclone.dto;

import com.example.spotifyclone.enums.ROLE;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CurrentUserDto {
    private Long id;
    private String name;
    private String email;
    private ROLE role;
}
