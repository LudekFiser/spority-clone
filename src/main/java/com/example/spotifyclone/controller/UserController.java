package com.example.spotifyclone.controller;

import com.example.spotifyclone.dto.ErrorDto;
import com.example.spotifyclone.dto.RegisterRequest;
import com.example.spotifyclone.dto.RegisterResponse;
import com.example.spotifyclone.enums.ROLE;
import com.example.spotifyclone.mapper.UserMapper;
import com.example.spotifyclone.repository.UserRepository;
import com.example.spotifyclone.service.EmailService;
import com.example.spotifyclone.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            UriComponentsBuilder uriBuilder) {
        var user =  userService.register(registerRequest);

        var uri = uriBuilder
                .path("/api/users/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(uri).body(user);
    }
}
