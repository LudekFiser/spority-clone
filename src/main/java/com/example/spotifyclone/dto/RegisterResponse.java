package com.example.spotifyclone.dto;

import com.example.spotifyclone.enums.ROLE;
import lombok.Builder;
import lombok.Data;



@Data
@Builder
public class RegisterResponse {

    private Long id;
    private String email;
    private String name;
    private ROLE role;
}
