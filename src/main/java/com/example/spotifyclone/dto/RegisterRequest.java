package com.example.spotifyclone.dto;

import com.example.spotifyclone.validation.Lowercase;
import com.example.spotifyclone.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required!")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @NotBlank(message = "Email is required!")
    @Email(message = "Enter a valid email...")
    @Lowercase(message = "Email must be in lowercase")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long!")
    @ValidPassword(message = "Password must contain at least one uppercase letter and one digit")
    private String password;
}
